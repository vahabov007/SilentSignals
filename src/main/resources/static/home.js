// Global variables
let currentUser = null;
let currentLocation = {
    latitude: null,
    longitude: null,
    address: null
};

// Initialize application
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupNavigation();
    getCurrentLocation();
    loadUserProfile();
    loadContacts();
});

// Initialize app
function initializeApp() {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        window.location.href = '/my-login';
        return;
    }
}

// Navigation
function setupNavigation() {
    const links = document.querySelectorAll('.nav-link');
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const target = this.getAttribute('href').substring(1);
            showSection(target);

            // Update active link
            links.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
        });
    });
}

function showSection(sectionId) {
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });

    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');
    }
}

// Location Services
function getCurrentLocation() {
    showLoading(true);

    if (!navigator.geolocation) {
        showError('Geolocation is not supported by your browser');
        showLoading(false);
        return;
    }

    navigator.geolocation.getCurrentPosition(
        async (position) => {
            currentLocation.latitude = position.coords.latitude;
            currentLocation.longitude = position.coords.longitude;

            document.getElementById('latitude').textContent = currentLocation.latitude.toFixed(6);
            document.getElementById('longitude').textContent = currentLocation.longitude.toFixed(6);

            await getAddressFromCoordinates(currentLocation.latitude, currentLocation.longitude);
            showLoading(false);
        },
        (error) => {
            handleLocationError(error);
            showLoading(false);
        },
        {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 60000
        }
    );
}

async function getAddressFromCoordinates(lat, lng) {
    try {
        const response = await fetch(
            `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`
        );

        if (response.ok) {
            const data = await response.json();
            currentLocation.address = data.display_name || 'Address not available';
            document.getElementById('address').textContent = currentLocation.address;
        } else {
            document.getElementById('address').textContent = 'Address service unavailable';
        }
    } catch (error) {
        document.getElementById('address').textContent = 'Unable to get address';
    }
}

function handleLocationError(error) {
    let message = 'Location error occurred';
    switch(error.code) {
        case error.PERMISSION_DENIED:
            message = 'Location access denied. Please enable location services.';
            break;
        case error.POSITION_UNAVAILABLE:
            message = 'Location information unavailable.';
            break;
        case error.TIMEOUT:
            message = 'Location request timed out.';
            break;
    }
    showError(message);
}

// SOS Alert with better error handling for Redis issues
async function sendSOS() {
    const description = document.getElementById('description').value.trim();

    if (!description) {
        showError('Please provide an emergency description');
        return;
    }

    if (!currentLocation.latitude || !currentLocation.longitude) {
        showError('Please wait for location to be acquired');
        return;
    }

    // Double confirmation for SOS
    if (!confirm('Are you sure you want to send an SOS alert? This will notify all your trusted contacts immediately.')) {
        return;
    }

    showLoading(true);

    try {
        const token = localStorage.getItem('jwtToken');
        const alertData = {
            description: description,
            locationCoordinates: `${currentLocation.latitude},${currentLocation.longitude}`,
            locationAddress: currentLocation.address || 'Address not available'
        };

        const response = await fetch('/api/alert/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(alertData)
        });

        const result = await response.json();

        if (result.success) {
            showSuccess('SOS alert sent successfully! Help is on the way!');
            document.getElementById('description').value = '';
        } else {
            // Handle specific error cases
            if (result.message && result.message.includes('Rate limit exceeded')) {
                const timeMatch = result.message.match(/(\d+) seconds/);
                const seconds = timeMatch ? timeMatch[1] : 'a few';
                showError(`Please wait ${seconds} seconds before sending another alert.`);
            } else if (result.message && result.message.includes('Redis')) {
                // Try fallback method or show user-friendly message
                showError('Alert system is temporarily unavailable. Please try again in a moment.');
                // You could implement a fallback here that saves alerts to database only
            } else {
                showError(result.message || 'Failed to send SOS alert');
            }
        }
    } catch (error) {
        console.error('SOS Error:', error);
        showError('Network error: Failed to send SOS alert. Please check your connection.');
    } finally {
        showLoading(false);
    }
}


// User Profile - FIXED: Correct element IDs
async function loadUserProfile() {
    try {
        const token = localStorage.getItem('jwtToken');
        const response = await fetch('/api/user/profile', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const result = await response.json();
            console.log('Profile response:', result); // Debug log
            if (result.success) {
                currentUser = result.data;
                displayUserProfile(currentUser);
            } else {
                showError('Failed to load profile: ' + (result.message || 'Unknown error'));
            }
        } else {
            console.error('Profile HTTP error:', response.status);
            if (response.status === 401) {
                localStorage.removeItem('jwtToken');
                window.location.href = '/my-login';
                return;
            }
            showError('Failed to load profile: HTTP ' + response.status);
        }
    } catch (error) {
        console.error('Profile load error:', error);
        showError('Network error loading profile: ' + error.message);
    }
}

function displayUserProfile(user) {
    console.log('Displaying user:', user); // Debug log

    // Update username in dashboard
    document.getElementById('username').textContent = user.username;

    // Update profile section
    document.getElementById('profileUsername').textContent = user.username;
    document.getElementById('profileEmail').textContent = user.email || user.mail;

    document.getElementById('profileId').textContent = user.id || '--';
    document.getElementById('profileDob').textContent = formatDate(user.dateOfBirth) || 'Not set';

    const statusBadge = document.getElementById('profileStatus');
    const emailBadge = document.getElementById('profileEmailVerified');

    statusBadge.textContent = user.enabled ? 'Active' : 'Inactive';
    statusBadge.className = `status-badge ${user.enabled ? 'active' : 'inactive'}`;

    const emailVerified = user.emailVerified !== undefined ? user.emailVerified : (user.isEmailVerified !== undefined ? user.isEmailVerified : false);
    emailBadge.textContent = emailVerified ? 'Verified' : 'Unverified';
    emailBadge.className = `status-badge ${emailVerified ? 'verified' : 'inactive'}`;

    document.getElementById('profileCreatedAt').textContent = formatDate(user.createdAt) || 'Unknown';
}

function formatDate(dateString) {
    if (!dateString) return null;
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    } catch (error) {
        console.error('Date format error:', error);
        return dateString;
    }
}

// Contacts Management
async function loadContacts() {
    try {
        const token = localStorage.getItem('jwtToken');
        const response = await fetch('/trusted/api/getAllContacts', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const result = await response.json();
        console.log('Contacts response:', result); // Debug log

        if (result.success) {
            displayContacts(result.data || []);
        } else {
            showError('Failed to load contacts: ' + (result.message || 'Unknown error'));
        }
    } catch (error) {
        console.error('Load contacts error:', error);
        showError('Network error loading contacts: ' + error.message);
    }
}


function displayContacts(contacts) {
    const container = document.getElementById('contactsList');
    console.log('Displaying contacts:', contacts); // Debug log

    if (!contacts || contacts.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-users"></i>
                <h3>No Trusted Contacts</h3>
                <p>Add trusted contacts to receive your SOS alerts</p>
            </div>
        `;
        return;
    }

    container.innerHTML = contacts.map(contact => `
        <div class="contact-item">
            <div class="contact-header">
                <div class="contact-name">${escapeHtml(contact.fullName)}</div>
                <span class="contact-type">${formatContactType(contact.contactType)}</span>
            </div>
            <div class="contact-details">
                ${contact.email ? ` <!-- FIXED: Changed from contact.mail to contact.email -->
                    <div class="contact-detail">
                        <i class="fas fa-envelope"></i>
                        <span>${escapeHtml(contact.email)}</span> <!-- FIXED: Changed from contact.mail to contact.email -->
                    </div>
                ` : ''}
                ${contact.phone ? `
                    <div class="contact-detail">
                        <i class="fas fa-phone"></i>
                        <span>${escapeHtml(contact.phone)}</span>
                    </div>
                ` : ''}
                ${contact.priorityOrder ? `
                    <div class="contact-detail">
                        <i class="fas fa-sort-numeric-down"></i>
                        <span>Priority: ${contact.priorityOrder}</span>
                    </div>
                ` : ''}
            </div>
            <div class="contact-actions">
                <button class="delete-btn" onclick="deleteContact(${contact.id})">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </div>
        </div>
    `).join('');
}

function formatContactType(contactType) {
    const typeMap = {
        'FAMILY': 'Family',
        'FRIEND': 'Friend',
        'EMERGENCY_CONTACT': 'Emergency Contact',
        'COLLEAGUE': 'Colleague',
        'NEIGHBOR': 'Neighbor',
        'OTHER': 'Other'
    };
    return typeMap[contactType] || contactType;
}

// FIXED: Delete contact uses userId instead of username
async function deleteContact(contactId) {
    if (!confirm('Are you sure you want to delete this contact?')) {
        return;
    }

    if (!currentUser || !currentUser.id) {
        showError('User information not available. Please refresh the page.');
        return;
    }

    showLoading(true);
    try {
        const token = localStorage.getItem('jwtToken');

        console.log(`Deleting contact: contactId=${contactId}`);

        // FIXED: Remove userId from URL - let backend get it from security context
        const response = await fetch(`/trusted/api/deleteContact/${contactId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const result = await response.json();
        console.log('Delete response:', result);

        if (result.success) {
            showSuccess('Contact deleted successfully!');
            loadContacts();
        } else {
            // More specific error messages
            if (result.message && result.message.includes('permission')) {
                showError('You do not have permission to delete this contact.');
            } else if (result.message && result.message.includes('not found')) {
                showError('Contact not found. It may have been already deleted.');
            } else {
                showError(result.message || 'Failed to delete contact');
            }
        }
    } catch (error) {
        console.error('Delete contact error:', error);
        showError('Network error: Failed to delete contact');
    } finally {
        showLoading(false);
    }
}

// Modal Functions
function showAddContactModal() {
    document.getElementById('addContactModal').style.display = 'block';
}

function closeModal() {
    document.getElementById('addContactModal').style.display = 'none';
    document.getElementById('contactForm').reset();
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('addContactModal');
    if (event.target === modal) {
        closeModal();
    }
}

// Contact Form Submission
// Contact Form Submission - Updated with optional phone
// Contact Form Submission - Updated to match new field name
document.getElementById('contactForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    if (!currentUser || !currentUser.username) {
        showError('User information not available. Please refresh the page.');
        return;
    }

    const formData = {
        fullName: document.getElementById('contactFullName').value.trim(),
        email: document.getElementById('contactMail').value.trim().toLowerCase(),
        phone: document.getElementById('contactPhone').value.trim(),
        contactType: document.getElementById('contactType').value,
        priorityOrder: parseInt(document.getElementById('contactPriority').value) || 1
    };

    // Validation
    if (!formData.fullName || !formData.email || !formData.contactType) {
        showError('Full name, email, and contact type are required');
        return;
    }

    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
        showError('Please enter a valid email address');
        return;
    }

    // Phone validation - only validate if provided
    if (formData.phone && formData.phone.trim() !== '') {
        const phoneRegex = /^\+?[0-9\-\s()]{10,}$/;
        if (!phoneRegex.test(formData.phone)) {
            showError('Please enter a valid phone number (minimum 10 digits) or leave it empty');
            return;
        }
    } else {
        // Set to null if empty
        formData.phone = null;
    }

    // Priority validation
    if (formData.priorityOrder < 1 || formData.priorityOrder > 10) {
        showError('Priority must be between 1 and 10');
        return;
    }

    showLoading(true);

    try {
        const token = localStorage.getItem('jwtToken');

        console.log('Adding trusted contact for current user:', formData);

        // FIXED: Remove username from URL - let backend get it from security context
        const response = await fetch('/trusted/api/addContact', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(formData)
        });

        const result = await response.json();
        console.log('Add contact response:', result);

        if (result.success) {
            showSuccess('Contact added successfully!');
            closeModal();
            loadContacts();
        } else {
            // Enhanced error handling
            if (result.data && typeof result.data === 'object') {
                const errors = Object.values(result.data).join(', ');
                showError('Please check your input: ' + errors);
            } else if (result.message && result.message.includes('already exists')) {
                showError('You already have a contact with this email address in your trusted contacts.');
            } else if (result.message && result.message.includes('User not found')) {
                showError('User account not found. Please try logging in again.');
            } else if (result.message && result.message.includes('system error')) {
                showError('A system error occurred. Please try again in a moment.');
            } else {
                showError(result.message || 'Failed to add contact. Please try again.');
            }
        }
    } catch (error) {
        console.error('Add contact error:', error);
        showError('Network error: Failed to add contact. Please check your connection.');
    } finally {
        showLoading(false);
    }
});

// Utility Functions
function showLoading(show) {
    document.getElementById('loading').style.display = show ? 'block' : 'none';
}

function showSuccess(message) {
    showNotification(message, 'success');
}

function showError(message) {
    showNotification(message, 'error');
}

function showNotification(message, type = 'info') {
    const container = document.getElementById('notificationContainer');
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
            <span>${message}</span>
        </div>
    `;

    container.appendChild(notification);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 5000);
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Logout
// Logout - Fixed version
function logout() {
    if (confirm('Are you sure you want to logout?')) {
        // Clear all stored data
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('userData');
        sessionStorage.clear();

        // Clear any cookies that might be set
        document.cookie.split(";").forEach(function(c) {
            document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
        });

        // Redirect to the correct login page
        window.location.href = '/my-login'; // Changed from '/custom-login' to '/my-login'
    }
}

// Make functions global
window.getCurrentLocation = getCurrentLocation;
window.sendSOS = sendSOS;
window.showAddContactModal = showAddContactModal;
window.closeModal = closeModal;
window.logout = logout;
window.deleteContact = deleteContact;