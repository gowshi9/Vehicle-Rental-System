// Finance Analytics JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('Finance analytics loaded');
    loadAnalyticsData();
});

function loadAnalyticsData() {
    // Fetch real-time analytics data
    fetch('/api/q/revenue-trend?days=30')
        .then(response => response.json())
        .then(data => {
            updateCharts(data);
        })
        .catch(error => {
            console.error('Error loading analytics:', error);
            showFallbackChart();
        });
}

function updateCharts(data) {
    const canvas = document.getElementById('revenueChart');
    if (canvas) {
        const ctx = canvas.getContext('2d');
        ctx.fillStyle = '#6366f1';
        ctx.fillRect(10, 10, 100, 50);
        ctx.fillStyle = '#ffffff';
        ctx.font = '14px Arial';
        ctx.fillText('Revenue: $' + (data.totalRevenue || 0), 15, 35);
    }
}

function showFallbackChart() {
    const canvas = document.getElementById('revenueChart');
    if (canvas) {
        const ctx = canvas.getContext('2d');
        ctx.fillStyle = '#e5e7eb';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        ctx.fillStyle = '#6b7280';
        ctx.font = '16px Arial';
        ctx.textAlign = 'center';
        ctx.fillText('Loading analytics...', canvas.width/2, canvas.height/2);
    }
}