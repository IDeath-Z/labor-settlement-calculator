package com.deathz.laborcalc;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Desktop;
import java.net.URI;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class LaborSettlementCalculatorApplication {

    private ConfigurableApplicationContext context;

    private static final String URL = "http://localhost:8080";

	public static void main(String[] args) {
		boolean isTest = Arrays.stream(args).anyMatch(arg -> arg.contains("test")) ||
                System.getProperty("spring.profiles.active", "").contains("test");

        if (!isTest) {
            System.setProperty("java.awt.headless", "false");
        }

        ConfigurableApplicationContext ctx = SpringApplication.run(LaborSettlementCalculatorApplication.class, args);

        LaborSettlementCalculatorApplication app = ctx.getBean(LaborSettlementCalculatorApplication.class);
        app.setContext(ctx);
	}

    public void setContext(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openWindow() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("Modo headless detectado - GUI desabilitada");
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(URL));
            }
        } catch (Exception ex) {
            System.err.println("Não foi possível abrir o navegador: " + ex.getMessage());
        }

        JFrame frame = new JFrame("Calculadora Laboral - API Rodando");
        frame.setSize(350, 150);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel statusLabel = new JLabel("API rodando e aberta no navegador!", SwingConstants.CENTER);
        statusLabel.setFont(statusLabel.getFont().deriveFont(14f));
        mainPanel.add(statusLabel);

        JButton closeButton = new JButton("Encerrar Aplicação");
        closeButton.addActionListener(e -> {
            context.close();
            System.exit(0);
        });
        mainPanel.add(closeButton);

        frame.add(mainPanel, BorderLayout.CENTER);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                context.close();
                System.exit(0);
            }
        });

        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
            frame.toFront();
            frame.requestFocus();
        });
    }
}
