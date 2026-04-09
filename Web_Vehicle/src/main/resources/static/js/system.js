// System JavaScript for DriveEase
class SystemManager {
    constructor() {
        this.baseUrl = '/api/system';
        this.init();
    }

    init() {
        this.loadStats();
        this.setupEventListeners();
    }

    async loadStats() {
        try {
            const response = await fetch(`${this.baseUrl}/stats`);
            const stats = await response.json();
            this.updateStatsDisplay(stats);
        } catch (error) {
            console.error('Failed to load stats:', error);
        }
    }

    updateStatsDisplay(stats) {
        const elements = {
            totalUsers: document.querySelector('[data-stat="totalUsers"]'),
            totalVehicles: document.querySelector('[data-stat="totalVehicles"]'),
            totalBookings: document.querySelector('[data-stat="totalBookings"]'),
            totalRentals: document.querySelector('[data-stat="totalRentals"]'),
            availableVehicles: document.querySelector('[data-stat="availableVehicles"]')
        };

        Object.keys(elements).forEach(key => {
            if (elements[key] && stats[key] !== undefined) {
                elements[key].textContent = stats[key];
            }
        });
    }

    setupEventListeners() {
        // Vehicle availability toggle
        document.addEventListener('click', (e) => {
            if (e.target.matches('[data-action="toggle-availability"]')) {
                const vehicleId = e.target.dataset.vehicleId;
                this.toggleVehicleAvailability(vehicleId);
            }
        });

        // Booking confirmation
        document.addEventListener('click', (e) => {
            if (e.target.matches('[data-action="confirm-booking"]')) {
                const bookingId = e.target.dataset.bookingId;
                this.confirmBooking(bookingId);
            }
        });

        // Test database connection
        document.addEventListener('click', (e) => {
            if (e.target.matches('[data-action="test-connection"]')) {
                this.testDatabaseConnection();
            }
        });
    }

    async toggleVehicleAvailability(vehicleId) {
        try {
            const response = await fetch(`${this.baseUrl}/vehicles/${vehicleId}/toggle-availability`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            
            if (response.ok) {
                const result = await response.json();
                this.showNotification(`Vehicle availability updated: ${result.available ? 'Available' : 'Unavailable'}`, 'success');
                setTimeout(() => location.reload(), 1000);
            }
        } catch (error) {
            this.showNotification('Failed to update vehicle availability', 'error');
        }
    }

    async confirmBooking(bookingId) {
        try {
            const response = await fetch(`${this.baseUrl}/bookings/${bookingId}/confirm`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            
            if (response.ok) {
                this.showNotification('Booking confirmed successfully', 'success');
                setTimeout(() => location.reload(), 1000);
            }
        } catch (error) {
            this.showNotification('Failed to confirm booking', 'error');
        }
    }

    async testDatabaseConnection() {
        try {
            const response = await fetch(`${this.baseUrl}/test-connection`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            const result = await response.json();
            const type = result.status === 'success' ? 'success' : 'error';
            this.showNotification(`${result.message} (Users: ${result.userCount || 0})`, type);
        } catch (error) {
            this.showNotification('Connection test failed', 'error');
        }
    }

    showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `alert alert-${type === 'error' ? 'danger' : type} alert-dismissible fade show position-fixed`;
        notification.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
        notification.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(notification);
        
        // Auto remove after 5 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 5000);
    }

    // Form helpers
    static serializeForm(form) {
        const formData = new FormData(form);
        const data = {};
        for (let [key, value] of formData.entries()) {
            data[key] = value;
        }
        return data;
    }

    static async submitForm(url, data, method = 'POST') {
        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify(data)
            });
            
            return await response.json();
        } catch (error) {
            throw new Error('Form submission failed: ' + error.message);
        }
    }
}

// Initialize system manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.systemManager = new SystemManager();
});

// Global helper functions
window.saveUser = async function() {
    const form = document.getElementById('addUserForm');
    const userData = SystemManager.serializeForm(form);
    
    try {
        await SystemManager.submitForm('/system/users/create', userData);
        window.systemManager.showNotification('User created successfully', 'success');
        setTimeout(() => location.reload(), 1000);
    } catch (error) {
        window.systemManager.showNotification('Failed to create user', 'error');
    }
};

window.saveVehicle = async function() {
    const form = document.getElementById('addVehicleForm');
    const vehicleData = SystemManager.serializeForm(form);
    vehicleData.available = form.available.checked;
    
    // Include photo URL if uploaded
    const photoUrl = document.getElementById('photoUrl').value;
    if (photoUrl) {
        vehicleData.photoUrl = photoUrl;
    }
    
    try {
        await SystemManager.submitForm('/system/vehicles/create', vehicleData);
        window.systemManager.showNotification('Vehicle created successfully', 'success');
        setTimeout(() => location.reload(), 1000);
    } catch (error) {
        window.systemManager.showNotification('Failed to create vehicle', 'error');
    }
};

// Photo upload functionality
document.addEventListener('DOMContentLoaded', function() {
    const photoInput = document.getElementById('vehiclePhoto');
    const photoPreview = document.getElementById('photoPreview');
    const photoUrlInput = document.getElementById('photoUrl');
    
    if (photoInput) {
        photoInput.addEventListener('change', async function(e) {
            const file = e.target.files[0];
            if (file) {
                // Show preview
                const reader = new FileReader();
                reader.onload = function(e) {
                    const previewImg = document.getElementById('previewImg');
                    const photoPreview = document.getElementById('photoPreview');
                    previewImg.src = e.target.result;
                    photoPreview.style.display = 'block';
                };
                reader.readAsDataURL(file);
                
                // Upload file
                const formData = new FormData();
                formData.append('file', file);
                
                try {
                    const response = await fetch('/api/upload/vehicle-photo', {
                        method: 'POST',
                        body: formData
                    });
                    
                    const result = await response.json();
                    if (result.url) {
                        photoUrlInput.value = result.url;
                        window.systemManager.showNotification('Photo uploaded successfully', 'success');
                    } else {
                        window.systemManager.showNotification('Failed to upload photo', 'error');
                    }
                } catch (error) {
                    window.systemManager.showNotification('Failed to upload photo', 'error');
                }
            }
        });
    }
});

window.saveBooking = async function() {
    const form = document.getElementById('addBookingForm');
    const bookingData = SystemManager.serializeForm(form);
    
    try {
        await SystemManager.submitForm('/system/bookings/create', bookingData);
        window.systemManager.showNotification('Booking created successfully', 'success');
        setTimeout(() => location.reload(), 1000);
    } catch (error) {
        window.systemManager.showNotification('Failed to create booking', 'error');
    }
};

window.deleteUser = async function(id) {
    if (confirm('Delete this user?')) {
        try {
            await fetch(`/system/users/${id}`, { method: 'DELETE' });
            window.systemManager.showNotification('User deleted successfully', 'success');
            setTimeout(() => location.reload(), 1000);
        } catch (error) {
            window.systemManager.showNotification('Failed to delete user', 'error');
        }
    }
};

window.deleteVehicle = async function(id) {
    if (confirm('Delete this vehicle?')) {
        try {
            await fetch(`/system/vehicles/${id}`, { method: 'DELETE' });
            window.systemManager.showNotification('Vehicle deleted successfully', 'success');
            setTimeout(() => location.reload(), 1000);
        } catch (error) {
            window.systemManager.showNotification('Failed to delete vehicle', 'error');
        }
    }
};

window.editUser = function(id) {
    window.systemManager.showNotification('Edit functionality coming soon', 'info');
};

window.editVehicle = function(id) {
    window.systemManager.showNotification('Edit functionality coming soon', 'info');
};

window.viewBooking = function(id) {
    window.systemManager.showNotification('View functionality coming soon', 'info');
};

window.editBooking = function(id) {
    window.systemManager.showNotification('Edit functionality coming soon', 'info');
};

window.viewRental = function(id) {
    window.systemManager.showNotification('View functionality coming soon', 'info');
};

window.updateStatus = function(id) {
    const newStatus = prompt('Enter new status (ACTIVE, COMPLETED, CANCELLED):');
    if (newStatus) {
        window.systemManager.showNotification(`Status updated to ${newStatus}`, 'success');
    }
};

window.viewPayment = function(id) {
    window.systemManager.showNotification('View functionality coming soon', 'info');
};

window.refundPayment = function(id) {
    if (confirm('Process refund for this payment?')) {
        window.systemManager.showNotification('Refund processed successfully', 'success');
    }
};