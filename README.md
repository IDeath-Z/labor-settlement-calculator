# Calculadora de Adicional Trabalhista

Calcula, mês a mês, o adicional de insalubridade proporcional devido ao longo de um vínculo, com correção pela Selic, e entrega o resultado como uma planilha `.xlsx` organizada ano a ano — o tipo de cálculo que vai anexado a um processo trabalhista.

O ponto do projeto não é o tamanho da feature. É o fato de que, num número que entra num processo, errar não é retrabalho: é credibilidade. Tudo aqui foi construído em torno de uma prioridade — **correção e auditabilidade acima de velocidade**.

## O problema

A conta em si é uma regra de domínio bagunçada. O adicional de insalubridade incide sobre o salário mínimo vigente, que muda de ano para ano. Sobre ele entram reflexos: 13º proporcional, férias mais um terço, FGTS. Há meses que não contam — rodízio de turno, período de pandemia. E o valor de cada competência precisa ser corrigido pela Selic acumulada até o fim do período. Feito à mão, em planilha, é o tipo de tarefa que consome de um a dois dias e onde um arredondamento errado passa despercebido.

A proposta foi transformar essa regra em software determinístico: mesma entrada, mesma saída, sempre, e com cada número rastreável até sua origem.

## O que isso já fez de concreto

Validado em seu primeiro caso real, substituiu o processo manual que levava de um a dois dias por um cálculo que termina em minutos. É o único resultado que afirmo — não há aqui métrica de volume, adoção por equipe ou benchmark de performance, porque isso não aconteceu.

## Decisões de engenharia

### Integridade de dado financeiro: falhar alto, nunca preencher com zero

Este é o ponto técnico que mais me importa no projeto.

Os valores de salário mínimo e Selic vêm da API de séries temporais do Banco Central. Uma API externa pode devolver uma série com buracos — uma competência ausente no meio do período. A reação ingênua seria tratar o mês faltante como zero e seguir adiante. Num cálculo que vai para um processo, isso é o pior resultado possível: um número errado que *parece* certo, sem nenhum sinal de que está errado.

A aplicação faz o oposto. Antes de calcular qualquer coisa, ela varre o período mês a mês e exige que ambas as séries cubram cada competência. Se faltar uma, ela para e aponta exatamente qual:

```java
private void validateSeriesCoverPeriod(SettlementInput input,
    Map<YearMonth, BigDecimal> wageMap, Map<YearMonth, BigDecimal> selicMap) {

    YearMonth current = YearMonth.from(input.startDate());
    YearMonth end = YearMonth.from(input.endDate());

    while (!current.isAfter(end)) {
        if (!wageMap.containsKey(current) || !selicMap.containsKey(current))
            throw new IncompleteSeriesException(current);

        current = current.plusMonths(1);
    }
}
```

É uma decisão intencional: prefiro uma falha visível, que nomeia a competência ausente (ex.: `2024-05`), a um resultado silenciosamente errado. Um cálculo que falha alto é confiável; um que corrige por baixo dos panos não é. Esse comportamento está amarrado em teste (`shouldThrowIncompleteSeriesExceptionWhenMonthIsMissing`).

### BigDecimal em todo o domínio — dinheiro nunca como `double`

Toda a aritmética monetária do domínio usa `BigDecimal` com `RoundingMode.HALF_UP` explícito a cada divisão. Não existe `double` no caminho do cálculo. O único lugar onde valores viram `double` é na fronteira de renderização da planilha, ao escrever uma célula no Apache POI — depois que o número já está fechado. O erro de ponto flutuante clássico (`0.1 + 0.2`) simplesmente não tem por onde entrar no resultado.

### Validação determinística com cenários fixos

A confiança no cálculo não vem de "rodei e pareceu certo". Vem de um conjunto de cenários com a saída correta esperada, conferida coluna a coluna.

O arquivo [`valid_results.csv`](src/test/resources/service/valid_results.csv) tem cada linha como um caso: período, salário, percentual de adicional e o valor esperado de cada parcela — adicional integral e proporcional, 13º, férias+1/3, FGTS, total, Selic acumulada, juros e total atualizado. Um teste parametrizado (`@CsvFileSource`) roda o serviço contra cada linha e compara as quatorze colunas. Mudar a regra de cálculo e quebrar um centavo de qualquer cenário derruba o build.

### Clean Architecture com a regra de dependência apontando para dentro

As camadas são explícitas e a dependência sempre aponta para o domínio:

- **`domain`** — o coração. `SettlementCalculatorService` é Java puro, sem uma única anotação de framework. Não sabe que Spring existe, não sabe que HTTP existe, não sabe o que é uma planilha. Recebe os dados de que precisa e devolve o resultado.
- **`application`** — os casos de uso (`CalculateLaborSettlementUseCase`, `GenerateReportUseCase`), que orquestram o domínio e dependem de interfaces, nunca de implementações concretas.
- **`infrastructure`** — os detalhes: os clients do Bacen, o gerador de planilha, a fiação de beans.
- **`presentation`** — controllers REST, DTOs, mapeamento de entrada.

O domínio não importa nada das camadas de fora. Quem depende de quem fica claro só de olhar os imports.

### Gateway Pattern: o domínio não sabe que o Bacen existe

O acesso ao Banco Central é abstraído por portas declaradas no domínio:

```java
public interface MinimumWageGateway {
    List<MinimumWage> getMinimumWageHistory(LocalDate startDate, LocalDate endDate);
}
```

A implementação (`BacenMinimumWageApiClient`, `BacenSelicApiClient`) vive na infraestrutura e lida com `HttpClient`, parsing de JSON e formato de data. O domínio recebe `List<MinimumWage>` e `List<SelicRate>` e ponto. Trocar a fonte de dados — outra API, um cache, um arquivo — não toca em uma linha de regra de negócio.

O gerador de relatório segue a mesma ideia: a porta `ReportGeneratorPort` expõe `supports(format)` e `generate(...)`, e o caso de uso escolhe a implementação pelo formato pedido. Hoje existe o gerador de planilha; somar um gerador de PDF é implementar a porta, sem mexer no resto.

### Tratamento de erro padronizado e que não vaza detalhe interno

Um `@RestControllerAdvice` central traduz exceções em respostas HTTP consistentes (`ApiErrorResponse`: timestamp, status, erro, mensagem, caminho). Violação de regra de negócio vira 400; falha ao obter dado do Bacen vira 502; falhas inesperadas viram 500 com uma mensagem genérica — o stack trace fica no log, não na resposta ao cliente. A `IncompleteSeriesException` descrita acima cai nesse mesmo handler e retorna **502 (Bad Gateway)** com a competência faltante na mensagem — o motivo exato (qual mês) chega até quem está usando, não um erro genérico.

## Como rodar

Pré-requisitos: **Java 21**. O Maven Wrapper já acompanha o projeto.

```bash
# build + testes
./mvnw clean package

# executar
java -jar target/labor-settlement-calculator-0.0.1-SNAPSHOT.jar
```

Ao subir, a aplicação abre o navegador sozinho em `http://localhost:8080` e mostra uma pequena janela de controle para encerrar o processo. Em ambiente sem interface gráfica (headless), ela detecta e segue só como API.

No formulário você preenche o período de apuração e o percentual de adicional (10 a 40), e opcionalmente marca **rodízio** (meses em que não há direito ao adicional, num ciclo) ou **período de pandemia** (intervalo desconsiderado). Ao calcular, o navegador baixa a planilha `.xlsx` já formatada, com uma linha por competência, resumo por ano e rodapé citando as séries do Banco Central usadas. Salário mínimo e Selic são buscados direto da API do Bacen pelas datas — não há tabela para atualizar na mão.

A interface estática (`src/main/resources/static`) faz a validação de entrada no próprio navegador antes de enviar.

## A conta, em resumo

Para cada mês do período, o serviço:

1. pega o salário mínimo vigente naquela competência;
2. calcula o adicional integral (`salário × percentual`) e, se o mês for parcial, o proporcional aos dias trabalhados;
3. deriva os reflexos sobre o proporcional — 13º (`/12`), férias mais um terço, FGTS (8%);
4. soma o total da competência;
5. acumula a Selic de forma composta da competência até o fim do período e aplica como correção.

Meses dentro de rodízio ou pandemia entram no resultado marcados, sem valor calculado, para que a planilha mostre o período inteiro sem fingir que aqueles meses geraram adicional. Ao final, os meses são agrupados por ano com totais principal e atualizado.

## Testes

```bash
./mvnw test
```

39 testes, todos passando. A cobertura é dirigida ao que pode dar errado num cálculo:

- **`SettlementCalculatorServiceTest`** — os 28 cenários do CSV conferidos coluna a coluna, mais os casos de rodízio, pandemia e série incompleta.
- **`CalculateLaborSettlementUseCaseTest`** — com Mockito, garante que o caso de uso reage quando o gateway devolve dado vazio.
- **`GenerateReportUseCaseTest`** — resultado vazio, formato não suportado e falha de geração.
- **`SpreadsheetReportGeneratorAdapterTest`** — seleção de formato pela porta.

## Stack

- Java 21
- Spring Boot 4.0.6 (web, actuator, validation)
- springdoc-openapi 2.5.0 (Swagger UI da API)
- Apache POI 5.2.4 (geração do `.xlsx`)
- Lombok (boilerplate de modelos e logging)
- `java.net.http.HttpClient` para a integração com o Bacen
- JUnit 5 + Mockito
- Maven (wrapper incluído)

## Estrutura

```
src/main/java/com/deathz/laborcalc
├── domain
│   ├── model        # SettlementInput, SettlementResult, MonthlyCompetenceDetail, ...
│   ├── service      # SettlementCalculatorService — a calculadora, Java puro
│   ├── ports        # MinimumWageGateway, SelicGateway, ReportGeneratorPort
│   ├── enums        # ReportFormat
│   └── exceptions   # IncompleteSeriesException
├── application
│   ├── usecases     # CalculateLaborSettlementUseCase, GenerateReportUseCase
│   └── exceptions   # BusinessRule, ExternalServiceNoDataFound, ReportGeneration (+ enums)
├── infrastructure
│   ├── client       # clients da API do Banco Central
│   ├── generator    # SpreadsheetReportGeneratorAdapter (Apache POI)
│   └── config       # fiação de beans
└── presentation
    ├── controllers  # SettlementController, GlobalExceptionHandler
    ├── dto
    └── mapper
```

## Demonstração

Formulário de entrada, com período, percentual e os controles de rodízio e período pandêmico:

![Formulário de cálculo](docs/input-form.png)

Planilha gerada, uma linha por competência com todas as parcelas e a correção pela Selic:

![Planilha detalhada](docs/detailed-spreadsheet.png)

Resumo por ano, com total principal e atualizado e o rodapé citando as séries do Banco Central:

![Resumo anual](docs/annual-summary.png)