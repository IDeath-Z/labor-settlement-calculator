const API_URL = '/api/v1/settlement/spreadsheet';
const blocks = { rodizio: false, pandemic: false };

const ERROR_MESSAGES = {
    REQUIRED_FIELD: 'Campo obrigatório',
    DATE_IS_BEFORE_RANGE: 'A data de fim deve ser após o início',
    DATE_IS_AFTER_RANGE: 'A data de início deve ser antes do fim',
    DATE_IS_BEFORE_TODAY: 'A data deve ser no passado',
    DATE_OUT_OF_BOUNDS: 'A data deve estar dentro do período de apuração',
    INVALID_INTERVAL: 'Intervalo deve ser de 1 a 12 meses',
    INVALID_PERCENTAGE: 'Selecione um percentual válido entre 10% e 40%'
};

function toggleBlock(name) {
    blocks[name] = !blocks[name];
    const block = document.getElementById('block-' + name);
    block.classList.toggle('active', blocks[name]);
    const inputs = block.querySelectorAll('input');
    inputs.forEach(inp => inp.disabled = !blocks[name]);
}

function validateAll() {
    const startDateVal = document.getElementById('startDate').value;
    const endDateVal = document.getElementById('endDate').value;
    
    let ok = true;
    
    const reqOk = validateRequiredFields();
    const rodizioOk = validateRodizio(startDateVal, endDateVal);
    const pandemicOk = validatePandemic(startDateVal, endDateVal);

    ok = reqOk && rodizioOk && pandemicOk;

    return ok;
}

function validateRequiredFields() {
    const startEl = document.getElementById('startDate');
    const endEl = document.getElementById('endDate');
    const pctEl = document.getElementById('additionalPct');
    const fieldStart = document.getElementById('field-start');
    const fieldEnd = document.getElementById('field-end');
    const fieldPct = document.getElementById('field-pct');
    fieldStart.classList.remove('has-error');
    fieldEnd.classList.remove('has-error');
    fieldPct.classList.remove('has-error');

    if (!startEl.value) {
        fieldStart.querySelector('.error-msg').textContent = ERROR_MESSAGES.REQUIRED_FIELD;
        fieldStart.classList.add('has-error');
        return false;
    }

    if (!endEl.value) {
        fieldEnd.querySelector('.error-msg').textContent = ERROR_MESSAGES.REQUIRED_FIELD;
        fieldEnd.classList.add('has-error');
        return false;
    }

    if (startEl.value && endEl.value && endEl.value <= startEl.value) {
        fieldEnd.querySelector('.error-msg').textContent = ERROR_MESSAGES.DATE_IS_BEFORE_RANGE;
        fieldEnd.classList.add('has-error');
        return false;
    }

    const pct = parseInt(pctEl.value);
    if (!pctEl.value || isNaN(pct) || pct < 10 || pct > 40) {
        fieldPct.querySelector('.error-msg').textContent = ERROR_MESSAGES.INVALID_PERCENTAGE;
        fieldPct.classList.add('has-error');
        return false;
    }
    return true;
}

function validateRodizio(baseStart, baseEnd) {
    if (blocks.rodizio) {
        let isValid = true;
        const fieldRs = document.getElementById('field-rotationStart');
        const fieldRi = document.getElementById('field-rotationInterval');
        const rsEl = document.getElementById('rotationStart');
        const riEl = document.getElementById('rotationInterval');
        
        fieldRs.classList.remove('has-error');
        fieldRi.classList.remove('has-error');

        if (!rsEl.value) {
            fieldRs.querySelector('.error-msg').textContent = ERROR_MESSAGES.REQUIRED_FIELD;
            fieldRs.classList.add('has-error');
            isValid = false;
        } else if (baseStart && (rsEl.value < baseStart || (baseEnd && rsEl.value > baseEnd))) {
            fieldRs.querySelector('.error-msg').textContent = ERROR_MESSAGES.DATE_OUT_OF_BOUNDS;
            fieldRs.classList.add('has-error');
            isValid = false;
        } 

        const interval = parseInt(riEl.value);
        if (!riEl.value || isNaN(interval) || interval < 1 || interval > 12) {
            fieldRi.querySelector('.error-msg').textContent = ERROR_MESSAGES.INVALID_INTERVAL;
            fieldRi.classList.add('has-error');
            isValid = false;
        }
        return isValid;
    }
    return true;
}

function validatePandemic(baseStart, baseEnd) {
    if (blocks.pandemic) {
        let isValid = true;
        const fieldPs = document.getElementById('field-pandemicStart');
        const fieldPe = document.getElementById('field-pandemicEnd');
        const psEl = document.getElementById('pandemicStart');
        const peEl = document.getElementById('pandemicEnd');

        fieldPs.classList.remove('has-error');
        fieldPe.classList.remove('has-error');

        if (!psEl.value) {
            fieldPs.querySelector('.error-msg').textContent = ERROR_MESSAGES.REQUIRED_FIELD;
            fieldPs.classList.add('has-error');
            isValid = false;
        } else if (baseStart && (psEl.value < baseStart || (baseEnd && psEl.value > baseEnd))) {
            fieldPs.querySelector('.error-msg').textContent = ERROR_MESSAGES.DATE_OUT_OF_BOUNDS;
            fieldPs.classList.add('has-error');
            isValid = false;
        }

        if (!peEl.value) {
            fieldPe.querySelector('.error-msg').textContent = ERROR_MESSAGES.REQUIRED_FIELD;
            fieldPe.classList.add('has-error');
            isValid = false;
        } else if (psEl.value && peEl.value < psEl.value) {
            fieldPe.querySelector('.error-msg').textContent = ERROR_MESSAGES.DATE_IS_BEFORE_RANGE; 
            fieldPe.classList.add('has-error');
            isValid = false;
        } else if (baseEnd && peEl.value > baseEnd) {
            fieldPe.querySelector('.error-msg').textContent = ERROR_MESSAGES.DATE_OUT_OF_BOUNDS;
            fieldPe.classList.add('has-error');
            isValid = false;
        }
        return isValid;
    }
    return true;
}

function buildJson() {
    const obj = {
        startDate: document.getElementById('startDate').value,
        endDate: document.getElementById('endDate').value,
        additionalPercentage: parseInt(document.getElementById('additionalPct').value)
    };

    if (blocks.rodizio) {
        const rs = document.getElementById('rotationStart').value;
        const ri = document.getElementById('rotationInterval').value;

        obj.shiftRotationStart = rs || null;
        obj.shiftRotationInterval = ri ? parseInt(ri) : null;
    } else {
        obj.shiftRotationStart = null;
        obj.shiftRotationInterval = null;
    }

    if (blocks.pandemic) {
        const ps = document.getElementById('pandemicStart').value;
        const pe = document.getElementById('pandemicEnd').value;

        obj.pandemicStartDate = ps || null;
        obj.pandemicEndDate = pe || null;
    } else {
        obj.pandemicStartDate = null;
        obj.pandemicEndDate = null;
    }

    return obj;
}

async function handleSubmit() {
    if (!validateAll()) return;

    const btn = document.querySelector('.btn-submit');
    const originalText = btn.textContent;
    btn.disabled = true;
    btn.textContent = 'Gerando arquivo...';

    const payload = buildJson();

    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            let backendMessage = `Erro no servidor (${response.status})`;
            try {
                const errorData = await response.json();
                if (errorData.message) {
                    backendMessage = errorData.message;
                }
            } catch (e) {
                // Default message will be used if response is not JSON or doesn't have 'message' field
            }
            throw new Error(backendMessage);
        }

        const blob = await response.blob();
        
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Calculo_Laboral_${Date.now()}.xlsx`;
        document.body.appendChild(a);
        a.click();
        
        a.remove();
        window.URL.revokeObjectURL(url);

        const banner = document.getElementById('success-banner');
        banner.style.display = 'flex';
        setTimeout(() => banner.style.display = 'none', 5000);

    } catch (error) {
        console.error('Falha na comunicação:', error);
        const errorBanner = document.getElementById('error-banner');
        errorBanner.innerHTML = `✖ &nbsp;${error.message}`;
        errorBanner.style.display = 'flex';
        setTimeout(() => errorBanner.style.display = 'none', 7000);
    } finally {
        btn.disabled = false;
        btn.textContent = originalText;
    }
}