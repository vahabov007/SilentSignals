document.addEventListener('DOMContentLoaded', function () {
    const loginForm = document.getElementById('login-form');
    const registrationForm = document.getElementById('registration-form');
    const mailInput = document.getElementById('mail');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const dateOfBirthInput = document.getElementById('dateOfBirth');
    const pinInput = document.getElementById('pin');
    const emailSection = document.getElementById('email-section');
    const pinVerificationArea = document.getElementById('pin-verification-area');
    const passwordCreationArea = document.getElementById('password-creation-area');
    const sendPinBtn = document.getElementById('send-pin-btn');
    const verifyPinBtn = document.getElementById('verify-pin-btn');
    const completeRegistrationBtn = document.getElementById('complete-registration-btn');
    const loadingOverlay = document.getElementById('loading-overlay');
    const loadingText = document.getElementById('loading-text');
    const emailErrorMessage = document.getElementById('email-error-message');
    const pinErrorMessage = document.getElementById('pin-error-message');
    const passwordErrorMessage = document.getElementById('password-error-message');
    const dateOfBirthError = document.getElementById('date-of-birth-error');
    const usernameErrorMessage = document.getElementById('username-error-message');
    const generalErrorMessage = document.getElementById('general-error-message');
    const errorMessage = document.getElementById('error-message');
    const existingAccountModal = document.getElementById('existing-account-modal');
    const modalCloseBtn = existingAccountModal ? existingAccountModal.querySelector('.close-btn') : null;
    const goToSigninBtn = document.getElementById('go-to-signin-btn');
    const pinTimer = document.getElementById('pin-timer');
    const resendPinBtn = document.getElementById('resend-pin-btn');

    let resendCount = 0;
    let timerInterval;
    let timerSeconds = 600; // 10 minutes

    // Password visibility toggle
    const passwordToggles = document.querySelectorAll('.password-toggle');
    passwordToggles.forEach(toggle => {
        toggle.addEventListener('click', function() {
            const input = this.parentElement.querySelector('input');
            if (!input) return;
            const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
            input.setAttribute('type', type);

            const icon = this.querySelector('i');
            if (!icon) return;
            if (type === 'password') {
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            } else {
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            }
        });
    });

    // Login process
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const usernameOrMail = document.getElementById('usernameOrMail').value.trim();
            const password = document.getElementById('password-sign-in').value;

            if (!usernameOrMail || !password) {
                showInlineMessage(errorMessage, 'Please enter both username/email and password.');
                return;
            }

            showLoading('Signing in...');

            fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username: usernameOrMail,
                    password: password
                })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                hideLoading();
                if (data.success) {
                    localStorage.setItem('jwtToken', data.token);
                    showInfoModal('Success', 'Login successful', true);
                    setTimeout(() => {
                        window.location.href = '/home';
                    }, 2000);
                } else {
                    showInlineMessage(errorMessage, data.message || 'Login failed');
                }
            })
            .catch(error => {
                hideLoading();
                console.error('Login Error:', error);
                showInlineMessage(errorMessage, 'An error occurred. Please try again.');
            });
        });
    }

    // PIN timer
    function startTimer() {
        timerSeconds = 600; // 10 minutes
        clearInterval(timerInterval);
        if (pinTimer) {
            pinTimer.textContent = formatTime(timerSeconds);
            pinTimer.style.display = 'block';
        }
        if (pinErrorMessage) {
            pinErrorMessage.style.display = 'none';
        }

        timerInterval = setInterval(() => {
            if (pinTimer) {
                pinTimer.textContent = formatTime(timerSeconds);
            }

            if (timerSeconds <= 0) {
                clearInterval(timerInterval);
                if (pinTimer) {
                    pinTimer.style.display = 'none';
                }
                if (pinErrorMessage) {
                    showInlineMessage(pinErrorMessage, 'PIN has expired. Please request a new one.');
                }
            }
            timerSeconds--;
        }, 1000);
    }

    function formatTime(seconds) {
        const minutes = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }

    // Message functions
    function showInlineMessage(element, message, isError = true) {
        if (element) {
            element.textContent = message;
            element.style.display = 'block';
            element.style.color = isError ? '#d9534f' : '#5cb85c';
        }
    }

    function hideInlineMessage(element) {
        if (element) {
            element.style.display = 'none';
            element.textContent = '';
        }
    }

    // Loading functions
    function showLoading(text) {
        if (loadingOverlay && loadingText) {
            loadingText.textContent = text;
            loadingOverlay.style.display = 'flex';
        }
    }

    function hideLoading() {
        if (loadingOverlay) {
            loadingOverlay.style.display = 'none';
        }
    }

    // Modal function
    function showInfoModal(title, message, isSuccess = true) {
        const modal = document.createElement('div');
        modal.className = 'modal';
        modal.innerHTML = `
            <div class="modal-content">
                <span class="info-close-btn">&times;</span>
                <div class="success-checkmark" style="display: ${isSuccess ? 'block' : 'none'};">
                    <div class="check-icon">
                        <span class="icon-line line-tip"></span>
                        <span class="icon-line line-long"></span>
                        <div class="icon-circle"></div>
                        <div class="icon-fix"></div>
                    </div>
                </div>
                <h3 style="color: ${isSuccess ? '#5cb85c' : '#d9534f'};">${title}</h3>
                <p>${message}</p>
            </div>
        `;
        document.body.appendChild(modal);
        setTimeout(() => {
            modal.style.display = 'flex';
        }, 10);
        modal.querySelector('.info-close-btn').addEventListener('click', () => {
            modal.style.display = 'none';
            modal.remove();
        });
        window.addEventListener('click', (event) => {
            if (event.target === modal) {
                modal.style.display = 'none';
                modal.remove();
            }
        });
    }

    // Registration process
    if (sendPinBtn) {
        sendPinBtn.addEventListener('click', function () {
            const mail = (mailInput?.value || '').trim();
            const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

            hideInlineMessage(emailErrorMessage);

            if (!mail) {
                showInlineMessage(emailErrorMessage, 'Please enter your email address.');
                return;
            }

            if (!emailRegex.test(mail)) {
                showInlineMessage(emailErrorMessage, 'Please enter a valid email address.');
                return;
            }

            showLoading('Sending PIN code...');

            fetch('/api/register/send-pin', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ mail: mail })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                hideLoading();
                if (data.success) {
                    if (emailSection) emailSection.style.display = 'none';
                    if (sendPinBtn) sendPinBtn.style.display = 'none';
                    if (pinVerificationArea) pinVerificationArea.style.display = 'block';
                    if (verifyPinBtn) verifyPinBtn.style.display = 'block';
                    resendCount = 0;
                    startTimer();
                    showInfoModal('Success', data.message, true);
                } else {
                    if (data.message && data.message.includes('already registered')) {
                        if (existingAccountModal) existingAccountModal.style.display = 'flex';
                    } else {
                        showInlineMessage(emailErrorMessage, data.message);
                    }
                }
            })
            .catch(error => {
                hideLoading();
                console.error('Send PIN Error:', error);
                showInlineMessage(emailErrorMessage, 'An error occurred. Please try again.');
            });
        });
    }

    if (verifyPinBtn) {
        verifyPinBtn.addEventListener('click', function () {
            const mail = (mailInput?.value || '').trim();
            const pin = (pinInput?.value || '').trim();

            hideInlineMessage(pinErrorMessage);

            if (!pin) {
                showInlineMessage(pinErrorMessage, 'Please enter the PIN code.');
                return;
            }

            if (pin.length !== 6) {
                showInlineMessage(pinErrorMessage, 'PIN must be 6 digits.');
                return;
            }

            showLoading('Verifying PIN...');

            fetch('/api/register/verify-pin', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ mail: mail, pin: pin })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                hideLoading();
                if (data.success) {
                    if (pinVerificationArea) pinVerificationArea.style.display = 'none';
                    if (passwordCreationArea) passwordCreationArea.style.display = 'block';
                    clearInterval(timerInterval);
                    if (pinTimer) pinTimer.style.display = 'none';
                    hideInlineMessage(pinErrorMessage);
                    showInfoModal('Success', data.message, true);
                } else {
                    showInlineMessage(pinErrorMessage, data.message);
                }
            })
            .catch(error => {
                hideLoading();
                console.error('Verify PIN Error:', error);
                showInlineMessage(pinErrorMessage, 'An error occurred. Please try again.');
            });
        });
    }

    if (completeRegistrationBtn) {
        completeRegistrationBtn.addEventListener('click', function (event) {
            const username = (usernameInput?.value || '').trim();
            const password = (passwordInput?.value || '');
            const confirmPassword = (confirmPasswordInput?.value || '');
            const mail = (mailInput?.value || '').trim();
            const dateInput = (dateOfBirthInput?.value || '');

            hideInlineMessage(usernameErrorMessage);
            hideInlineMessage(passwordErrorMessage);
            hideInlineMessage(dateOfBirthError);
            hideInlineMessage(generalErrorMessage);

            // Validation
            if (!username || !password || !confirmPassword || !dateInput) {
                showInlineMessage(generalErrorMessage, 'All fields are required.');
                return;
            }

            if (username.length < 3 || username.length > 40) {
                showInlineMessage(usernameErrorMessage, 'Username must be between 3 and 40 characters long.');
                return;
            }

            if (password.length < 8) {
                showInlineMessage(passwordErrorMessage, 'Password must be at least 8 characters long.');
                return;
            }

            if (!password.match(/.*\d.*/)) {
                showInlineMessage(passwordErrorMessage, 'Password must contain at least one number.');
                return;
            }

            if (password !== confirmPassword) {
                showInlineMessage(passwordErrorMessage, 'Passwords do not match.');
                return;
            }

            const today = new Date();
            const birthDate = new Date(dateInput);

            if (isNaN(birthDate.getTime())) {
                showInlineMessage(dateOfBirthError, 'Please enter a valid date of birth.');
                return;
            }

            let minDate = new Date();
            minDate.setFullYear(minDate.getFullYear() - 18);
            if (birthDate > minDate) {
                showInlineMessage(dateOfBirthError, 'You must be at least 18 years old to register.');
                return;
            }

            let maxDate = new Date();
            maxDate.setFullYear(maxDate.getFullYear() - 125);
            if (birthDate < maxDate) {
                showInlineMessage(dateOfBirthError, 'Please enter a valid date of birth.');
                return;
            }

            showLoading('Completing registration...');

            const registrationData = {
                mail: mail,
                username: username,
                password: password,
                confirmPassword: confirmPassword,
                dateOfBirth: dateInput
            };

            fetch('/api/register/complete-registration', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(registrationData)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                hideLoading();
                if (data.success) {
                    showInfoModal('Success', data.message, true);
                    setTimeout(() => {
                        window.location.href = '/my-login';
                    }, 3000);
                } else {
                    showInlineMessage(generalErrorMessage, data.message);
                }
            })
            .catch(error => {
                hideLoading();
                console.error('Registration Error:', error);
                showInlineMessage(generalErrorMessage, 'An error occurred. Please try again.');
            });
        });
    }

    if (resendPinBtn) {
        resendPinBtn.addEventListener('click', function () {
            const mail = (mailInput?.value || '').trim();

            if (!mail) {
                showInlineMessage(pinErrorMessage, 'Please go back and enter your email address.', true);
                return;
            }

            if (resendCount >= 3) {
                pinErrorMessage.innerHTML = '<span style="color: #d9534f;">You have exceeded the maximum number of resend attempts. Please <a href="https://vahabvahabov.site/#contact" target="_blank">contact support</a> for assistance.</span>';
                pinErrorMessage.style.display = 'block';
                console.log('Resend blocked: Max attempts reached.');
                return;
            }

            resendPinBtn.disabled = true;
            showLoading('Resending PIN...');

            fetch('/api/register/resend-pin', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ mail: mail })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                hideLoading();
                if (data.success) {
                    resendCount++;
                    startTimer();
                    showInfoModal('Success', 'A new PIN has been sent to your email address.', true);
                } else {
                    showInlineMessage(pinErrorMessage, data.message, true);
                }
                resendPinBtn.disabled = false;
            })
            .catch(error => {
                hideLoading();
                console.error('Resend PIN Error:', error);
                showInlineMessage(pinErrorMessage, 'An error occurred. Please try again.', true);
                resendPinBtn.disabled = false;
            });
        });
    }

    if (modalCloseBtn) {
        modalCloseBtn.addEventListener('click', function () {
            if (existingAccountModal) existingAccountModal.style.display = 'none';
        });
    }

    if (goToSigninBtn) {
        goToSigninBtn.addEventListener('click', function () {
            window.location.href = '/my-login';
        });
    }
});