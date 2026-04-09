// Common JavaScript utilities for Vehicle Rental System

// CSRF Token handling
function getCSRFToken() {
    const token = document.querySelector('meta[name="_csrf"]');
    return token ? token.getAttribute('content') : '';
}

function getCSRFHeader() {
    const header = document.querySelector('meta[name="_csrf_header"]');
    return header ? header.getAttribute('content') : 'X-CSRF-TOKEN';
}

// Fetch wrapper with CSRF and JSON handling
async function apiCall(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            [getCSRFHeader()]: getCSRFToken()
        }
    };
    
    const config = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers
        }
    };
    
    try {
        const response = await fetch(url, config);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error('API call failed:', error);
        throw error;
    }
}

// Form validation helpers
function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function validatePhone10(phone) {
    const phoneRegex = /^\d{10}$/;
    return phoneRegex.test(phone.replace(/\D/g, ''));
}

function validatePasswordPolicy(password) {
    const minLength = password.length >= 8;
    const hasUpper = /[A-Z]/.test(password);
    const hasNumber = /\d/.test(password);
    const hasSymbol = /[!@#$%^&*(),.?":{}|<>]/.test(password);
    
    return {
        valid: minLength && hasUpper && hasNumber && hasSymbol,
        minLength,
        hasUpper,
        hasNumber,
        hasSymbol
    };
}

function getPasswordStrength(password) {
    const validation = validatePasswordPolicy(password);
    let score = 0;
    
    if (validation.minLength) score++;
    if (validation.hasUpper) score++;
    if (validation.hasNumber) score++;
    if (validation.hasSymbol) score++;
    
    if (score <= 2) return 'weak';
    if (score === 3) return 'medium';
    return 'strong';
}

// Error handling
function showError(element, message) {
    clearErrors(element);
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error';
    errorDiv.textContent = message;
    element.parentNode.appendChild(errorDiv);
    element.style.borderColor = 'var(--danger)';
}

function showSuccess(element, message) {
    clearErrors(element);
    const successDiv = document.createElement('div');
    successDiv.className = 'success';
    successDiv.textContent = message;
    element.parentNode.appendChild(successDiv);
    element.style.borderColor = 'var(--success)';
}

function clearErrors(element) {
    const parent = element.parentNode;
    const existingErrors = parent.querySelectorAll('.error, .success');
    existingErrors.forEach(error => error.remove());
    element.style.borderColor = '';
}

// Toast notifications
function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => toast.classList.add('show'), 100);
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => document.body.removeChild(toast), 300);
    }, 3000);
}

// Password strength meter
function initPasswordStrength(passwordInput, confirmInput = null) {
    const strengthMeter = document.createElement('div');
    strengthMeter.className = 'password-strength';
    const strengthBar = document.createElement('div');
    strengthBar.className = 'password-strength-bar';
    strengthMeter.appendChild(strengthBar);
    passwordInput.parentNode.appendChild(strengthMeter);
    
    const strengthText = document.createElement('div');
    strengthText.className = 'password-strength-text';
    strengthMeter.parentNode.appendChild(strengthText);
    
    passwordInput.addEventListener('input', function() {
        const password = this.value;
        const validation = validatePasswordPolicy(password);
        const strength = getPasswordStrength(password);
        
        // Update strength bar
        let width = 0;
        let className = '';
        
        if (password.length > 0) {
            if (strength === 'weak') {
                width = 33;
                className = 'strength-weak';
            } else if (strength === 'medium') {
                width = 66;
                className = 'strength-medium';
            } else {
                width = 100;
                className = 'strength-strong';
            }
        }
        
        strengthBar.style.width = width + '%';
        strengthBar.className = `password-strength-bar ${className}`;
        
        // Update text
        if (password.length === 0) {
            strengthText.textContent = '';
        } else {
            const requirements = [];
            if (!validation.minLength) requirements.push('8+ characters');
            if (!validation.hasUpper) requirements.push('uppercase letter');
            if (!validation.hasNumber) requirements.push('number');
            if (!validation.hasSymbol) requirements.push('symbol');
            
            if (requirements.length === 0) {
                strengthText.textContent = 'Strong password';
                strengthText.className = 'password-strength-text success';
            } else {
                strengthText.textContent = `Missing: ${requirements.join(', ')}`;
                strengthText.className = 'password-strength-text error';
            }
        }
        
        // Validate confirm password if provided
        if (confirmInput && confirmInput.value) {
            validatePasswordConfirm(passwordInput, confirmInput);
        }
    });
    
    if (confirmInput) {
        confirmInput.addEventListener('input', function() {
            validatePasswordConfirm(passwordInput, confirmInput);
        });
    }
}

function validatePasswordConfirm(passwordInput, confirmInput) {
    if (passwordInput.value !== confirmInput.value) {
        showError(confirmInput, 'Passwords do not match');
    } else if (confirmInput.value.length > 0) {
        showSuccess(confirmInput, 'Passwords match');
    } else {
        clearErrors(confirmInput);
    }
}

// Form validation on submit
function validateForm(form) {
    let isValid = true;
    
    // Email validation
    const emailInputs = form.querySelectorAll('input[type="email"]');
    emailInputs.forEach(input => {
        if (input.value && !validateEmail(input.value)) {
            showError(input, 'Please enter a valid email address');
            isValid = false;
        }
    });
    
    // Phone validation
    const phoneInputs = form.querySelectorAll('input[name="phone"]');
    phoneInputs.forEach(input => {
        if (input.value && !validatePhone10(input.value)) {
            showError(input, 'Phone number must be exactly 10 digits');
            isValid = false;
        }
    });
    
    // Password validation
    const passwordInputs = form.querySelectorAll('input[type="password"][name="password"]');
    passwordInputs.forEach(input => {
        if (input.value && !validatePasswordPolicy(input.value).valid) {
            showError(input, 'Password must be at least 8 characters with uppercase, number, and symbol');
            isValid = false;
        }
    });
    
    // Date validation
    const startDateInputs = form.querySelectorAll('input[name="startDate"]');
    const endDateInputs = form.querySelectorAll('input[name="endDate"]');
    
    if (startDateInputs.length > 0 && endDateInputs.length > 0) {
        const startDate = new Date(startDateInputs[0].value);
        const endDate = new Date(endDateInputs[0].value);
        
        if (startDate >= endDate) {
            showError(endDateInputs[0], 'End date must be after start date');
            isValid = false;
        }
    }
    
    return isValid;
}

// Initialize form validation
document.addEventListener('DOMContentLoaded', function() {
    // Auto-initialize password strength meters
    const passwordInputs = document.querySelectorAll('input[type="password"][name="password"]');
    passwordInputs.forEach(passwordInput => {
        const confirmInput = document.querySelector('input[name="confirmPassword"]');
        initPasswordStrength(passwordInput, confirmInput);
    });
    
    // Auto-validate forms on submit
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!validateForm(this)) {
                e.preventDefault();
                showToast('Please fix the errors in the form', 'error');
            }
        });
    });
    
    // Real-time validation
    const inputs = document.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
        input.addEventListener('blur', function() {
            if (this.type === 'email' && this.value) {
                if (!validateEmail(this.value)) {
                    showError(this, 'Please enter a valid email address');
                } else {
                    clearErrors(this);
                }
            }
            
            if (this.name === 'phone' && this.value) {
                if (!validatePhone10(this.value)) {
                    showError(this, 'Phone number must be exactly 10 digits');
                } else {
                    clearErrors(this);
                }
            }
        });
    });
});

// Utility functions for date handling
function formatDate(date) {
    return new Date(date).toLocaleDateString();
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(amount);
}

// Export functions for use in other scripts
window.VehicleRental = {
    apiCall,
    validateEmail,
    validatePhone10,
    validatePasswordPolicy,
    showError,
    showSuccess,
    clearErrors,
    showToast,
    formatDate,
    formatCurrency
};