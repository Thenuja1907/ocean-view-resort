/**
 * Ocean View Resort — Dashboard Application Logic
 */

document.addEventListener('DOMContentLoaded', () => {
    initDashboard();
    initEventListeners();
});

// ── INITIALISATION ───────────────────────────────────────────────────────

async function initDashboard() {
    await updateCounts();
    await loadRecentReservations();
    await loadRoomGrid();
}

function initEventListeners() {
    // Nav Toggling
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const section = link.getAttribute('data-section');
            showSection(section);
        });
    });

    // Modal Control
    const modal = document.getElementById('bookingModal');
    const btnBooking = document.getElementById('btnQuickBooking');
    const btnClose = document.querySelector('.close-modal');

    btnBooking.addEventListener('click', () => {
        modal.classList.add('active');
        prepareBookingForm();
    });

    btnClose.addEventListener('click', () => modal.classList.remove('active'));

    // Form Submission
    document.getElementById('bookingForm').addEventListener('submit', handleBooking);

    // Logout
    document.getElementById('logoutBtn').addEventListener('click', logout);
}

// ── DATA FETCHING ────────────────────────────────────────────────────────

async function updateCounts() {
    try {
        // Fetch rooms to count availability
        const respRooms = await fetch('api/rooms/available');
        const rooms = (await respRooms.json()).data;
        document.getElementById('countAvailable').textContent = rooms.length;

        // Fetch reservations
        const respRes = await fetch('api/reservations');
        const resList = (await respRes.json()).data;
        document.getElementById('countActive').textContent =
            resList.filter(r => r.status === 'CONFIRMED' || r.status === 'CHECKED_IN').length;

        // Today's checkins (mock logic for demo: filter by today's date)
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('countCheckins').textContent =
            resList.filter(r => r.checkInDate === today).length;

        // Pending bills
        const respBills = await fetch('api/bills');
        const bills = (await respBills.json()).data;
        document.getElementById('countPendingBills').textContent =
            bills.filter(b => b.paymentStatus === 'PENDING').length;

    } catch (err) { console.error('Count update failed', err); }
}

async function loadRecentReservations() {
    const tbody = document.getElementById('recentReservationsBody');
    try {
        const resp = await fetch('api/reservations');
        const data = await resp.json();
        const list = data.data.slice(0, 5); // top 5

        tbody.innerHTML = list.map(r => `
            <tr>
                <td><strong>${r.reservationNumber}</strong></td>
                <td>Guest ID: ${r.guestId}</td>
                <td>Room ID: ${r.roomId}</td>
                <td>${r.checkInDate}</td>
                <td><span class="status-badge status-${r.status.toLowerCase()}">${r.status}</span></td>
            </tr>
        `).join('');
    } catch (err) { tbody.innerHTML = '<tr><td colspan="5">Error loading data</td></tr>'; }
}

async function loadRoomGrid() {
    const grid = document.getElementById('roomStatusGrid');
    try {
        const resp = await fetch('api/rooms');
        const rooms = (await resp.json()).data;

        grid.innerHTML = rooms.map(room => `
            <div class="room-node ${room.available ? 'available' : 'occupied'}" title="${room.roomType}">
                ${room.roomNumber}
            </div>
        `).join('');
    } catch (err) { grid.innerHTML = 'Error'; }
}

// ── FORM LOGIC ───────────────────────────────────────────────────────────

async function prepareBookingForm() {
    try {
        const [respGuests, respRooms] = await Promise.all([
            fetch('api/guests'),
            fetch('api/rooms/available')
        ]);

        const guests = (await respGuests.json()).data;
        const rooms = (await respRooms.json()).data;

        const gSelect = document.getElementById('guestSelect');
        const rSelect = document.getElementById('roomSelect');

        gSelect.innerHTML = guests.map(g => `<option value="${g.guestId}">${g.firstName} ${g.lastName}</option>`).join('');
        rSelect.innerHTML = rooms.map(r => `<option value="${r.roomId}">${r.roomNumber} (${r.roomType})</option>`).join('');

        // Set default dates
        const inDate = new Date();
        const outDate = new Date();
        outDate.setDate(inDate.getDate() + 1);

        document.getElementById('checkInDate').value = inDate.toISOString().split('T')[0];
        document.getElementById('checkOutDate').value = outDate.toISOString().split('T')[0];

    } catch (err) { alert('Failed to load form data'); }
}

async function handleBooking(e) {
    e.preventDefault();
    const formData = new URLSearchParams();
    formData.append('guestId', document.getElementById('guestSelect').value);
    formData.append('roomId', document.getElementById('roomSelect').value);
    formData.append('checkInDate', document.getElementById('checkInDate').value);
    formData.append('checkOutDate', document.getElementById('checkOutDate').value);
    formData.append('numGuests', document.getElementById('numGuests').value);
    formData.append('specialRequests', document.getElementById('specialRequests').value);

    try {
        const resp = await fetch('api/reservations', {
            method: 'POST',
            body: formData
        });
        const res = await resp.json();

        if (res.success) {
            alert('Reservation successful! Res #: ' + res.data.reservationNumber);
            document.getElementById('bookingModal').classList.remove('active');
            initDashboard();
        } else {
            alert('Error: ' + res.message);
        }
    } catch (err) { alert('Network error'); }
}

// ── HELPERS ─────────────────────────────────────────────────────────────

function showSection(sectionId) {
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    document.querySelector(`[data-section="${sectionId}"]`).classList.add('active');

    document.getElementById('sectionTitle').textContent =
        sectionId.charAt(0).toUpperCase() + sectionId.slice(1) + ' Management';
}

async function logout() {
    await fetch('api/auth/logout');
    window.location.href = 'login.html';
}
