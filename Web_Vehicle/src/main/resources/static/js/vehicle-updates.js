// Real-time vehicle updates using Server-Sent Events
class VehicleUpdates {
    constructor() {
        this.eventSource = null;
        this.init();
    }

    init() {
        if (typeof(EventSource) !== "undefined") {
            this.eventSource = new EventSource('/api/vehicles/updates');
            
            this.eventSource.addEventListener('vehicle-update', (event) => {
                const data = JSON.parse(event.data);
                this.handleVehicleUpdate(data);
            });

            this.eventSource.onerror = (error) => {
                console.log('SSE connection error:', error);
            };
        }
    }

    handleVehicleUpdate(data) {
        const { action, vehicleId } = data;
        
        if (action === 'created' || action === 'updated') {
            this.refreshVehicleList();
        }
    }

    refreshVehicleList() {
        // Refresh vehicle catalog if on vehicles page
        if (window.location.pathname === '/vehicles' || window.location.pathname.includes('/vehicles')) {
            this.loadVehicles();
        }
    }

    async loadVehicles() {
        try {
            const response = await fetch('/api/vehicles');
            const vehicles = await response.json();
            this.updateVehicleDisplay(vehicles);
        } catch (error) {
            console.error('Error loading vehicles:', error);
        }
    }

    updateVehicleDisplay(vehicles) {
        const vehicleGrid = document.querySelector('.vehicle-grid');
        if (!vehicleGrid) return;

        vehicleGrid.innerHTML = vehicles.map(vehicle => `
            <div class="vehicle-card" data-vehicle-id="${vehicle.id}">
                <div class="vehicle-image">
                    <img src="${vehicle.photoUrl || '/images/default-car.jpg'}" alt="${vehicle.make} ${vehicle.model}">
                </div>
                <div class="vehicle-info">
                    <h3>${vehicle.make} ${vehicle.model}</h3>
                    <p class="vehicle-year">${vehicle.year}</p>
                    <p class="vehicle-category">${vehicle.category}</p>
                    <div class="vehicle-price">$${vehicle.dailyRate}/day</div>
                    <a href="/vehicles/${vehicle.id}" class="btn btn-primary">View Details</a>
                </div>
            </div>
        `).join('');
    }

    disconnect() {
        if (this.eventSource) {
            this.eventSource.close();
        }
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.vehicleUpdates = new VehicleUpdates();
});

// Cleanup on page unload
window.addEventListener('beforeunload', () => {
    if (window.vehicleUpdates) {
        window.vehicleUpdates.disconnect();
    }
});