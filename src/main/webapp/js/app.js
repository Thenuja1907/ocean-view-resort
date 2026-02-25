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

    // Booking Modal Control
    const bookingModal = document.getElementById('bookingModal');
    const btnBooking = document.getElementById('btnQuickBooking');
    const btnCloseBooking = document.querySelector('.close-modal');

    if (btnBooking) {
        btnBooking.addEventListener('click', () => {
            bookingModal.classList.add('active');
            prepareBookingForm();
        });
    }

    if (btnCloseBooking) {
        btnCloseBooking.addEventListener('click', () => bookingModal.classList.remove('active'));
    }

    // Guest Modal Control
    const guestModal = document.getElementById('guestModal');
    const btnAddGuest = document.getElementById('btnAddGuest');
    const btnCloseGuest = document.getElementById('btnCloseGuestModal');

    if (btnAddGuest) {
        btnAddGuest.addEventListener('click', () => {
            guestModal.classList.add('active');
        });
    }

    if (btnCloseGuest) {
        btnCloseGuest.addEventListener('click', () => guestModal.classList.remove('active'));
    }

    // Form Submissions
    const bookingForm = document.getElementById('bookingForm');
    if (bookingForm) {
        bookingForm.addEventListener('submit', handleBooking);
    }

    const guestForm = document.getElementById('guestForm');
    if (guestForm) {
        guestForm.addEventListener('submit', handleAddGuest);
    }

    // Logout
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }
}

// ── DATA FETCHING ────────────────────────────────────────────────────────

async function updateCounts() {
    try {
        const respRooms = await fetch('api/rooms/available');
        if (respRooms.status === 401) {
            window.location.href = 'staff_login.html';
            return;
        }
        const roomsJson = await respRooms.json();
        const rooms = roomsJson.data || [];
        document.getElementById('countAvailable').textContent = rooms.length;

        const respRes = await fetch('api/reservations');
        const resJson = await respRes.json();
        const resList = resJson.data || [];
        document.getElementById('countActive').textContent =
            resList.filter(r => r.status === 'CONFIRMED' || r.status === 'CHECKED_IN').length;

        // Check-in count (Today local date)
        const now = new Date();
        const offset = now.getTimezoneOffset();
        const localToday = new Date(now.getTime() - (offset * 60 * 1000)).toISOString().split('T')[0];

        document.getElementById('countCheckins').textContent =
            resList.filter(r => r.checkInDate === localToday && r.status !== 'CANCELLED').length;

        const respBills = await fetch('api/bills');
        const billsJson = await respBills.json();
        const bills = billsJson.data || [];
        document.getElementById('countPendingBills').textContent =
            bills.filter(b => b.paymentStatus === 'PENDING').length;

    } catch (err) { console.error('Count update failed', err); }
}

async function loadRecentReservations() {
    const tbody = document.getElementById('recentReservationsBody');
    if (!tbody) return;
    try {
        const resp = await fetch('api/reservations');
        const data = await resp.json();
        const list = (data.data || []).slice(0, 5); // top 5

        tbody.innerHTML = list.map(r => `
            <tr>
                <td><strong>${r.reservationNumber}</strong></td>
                <td>${r.guest ? r.guest.firstName + ' ' + r.guest.lastName : 'Guest ID: ' + r.guestId}</td>
                <td>${r.room ? 'Room ' + r.room.roomNumber : 'Room ID: ' + r.roomId}</td>
                <td>${r.checkInDate}</td>
                <td><span class="status-badge status-${r.status.toLowerCase()}">${r.status}</span></td>
            </tr>
        `).join('');
    } catch (err) { tbody.innerHTML = '<tr><td colspan="5">Error loading data</td></tr>'; }
}

async function loadRoomGrid() {
    const grid = document.getElementById('roomStatusGrid');
    if (!grid) return;
    try {
        const resp = await fetch('api/rooms');
        const data = await resp.json();
        const rooms = data.data || [];

        grid.innerHTML = rooms.map(room => `
            <div class="room-node ${room.available ? 'available' : 'occupied'}" title="${room.roomType}">
                ${room.roomNumber}
            </div>
        `).join('');
    } catch (err) { grid.innerHTML = 'Error'; }
}

async function loadGuestList() {
    const tbody = document.getElementById('guestListBody');
    if (!tbody) return;
    try {
        const resp = await fetch('api/guests');
        const data = await resp.json();
        const list = data.data || [];

        if (list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7">No guests registered.</td></tr>';
            return;
        }

        tbody.innerHTML = list.map(g => `
            <tr>
                <td>${g.guestId}</td>
                <td>${g.firstName} ${g.lastName}</td>
                <td>${g.email}</td>
                <td>${g.contactNumber}</td>
                <td>${g.idNumber} (${g.idType})</td>
                <td>${g.nationality}</td>
                <td>
                    <button class="btn btn-sm btn-outline" title="Edit Guest"><i class="fas fa-edit"></i></button>
                </td>
            </tr>
        `).join('');
    } catch (err) { tbody.innerHTML = '<tr><td colspan="7">Error loading guests.</td></tr>'; }
}

// ── FORM LOGIC ───────────────────────────────────────────────────────────

async function prepareBookingForm() {
    try {
        const [respGuests, respRooms] = await Promise.all([
            fetch('api/guests'),
            fetch('api/rooms/available')
        ]);

        const guestsJson = await respGuests.json();
        const roomsJson = await respRooms.json();
        const guests = guestsJson.data || [];
        const rooms = roomsJson.data || [];

        const gSelect = document.getElementById('guestSelect');
        const rSelect = document.getElementById('roomSelect');

        gSelect.innerHTML = guests.map(g => `<option value="${g.guestId}">${g.firstName} ${g.lastName}</option>`).join('');
        rSelect.innerHTML = rooms.map(r => `<option value="${r.roomId}">${r.roomNumber} (${r.roomType})</option>`).join('');

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

async function handleAddGuest(e) {
    e.preventDefault();
    const formData = new URLSearchParams();
    formData.append('firstName', document.getElementById('guestFirst').value);
    formData.append('lastName', document.getElementById('guestLast').value);
    formData.append('email', document.getElementById('guestEmail').value);
    formData.append('contactNumber', document.getElementById('guestPhone').value);
    formData.append('address', document.getElementById('guestAddress').value);
    formData.append('nationality', document.getElementById('guestNationality').value);
    formData.append('idType', document.getElementById('guestIdType').value);
    formData.append('idNumber', document.getElementById('guestIdNum').value);
    formData.append('password', document.getElementById('guestPassword').value);

    try {
        const resp = await fetch('api/guests', {
            method: 'POST',
            body: formData
        });
        const res = await resp.json();

        if (res.success) {
            alert('Guest added successfully!');
            document.getElementById('guestForm').reset();
            document.getElementById('guestModal').classList.remove('active');
            loadGuestList();
        } else {
            alert('Error: ' + res.message);
        }
    } catch (err) { alert('Network error'); }
}

// ── HELPERS ─────────────────────────────────────────────────────────────

function showSection(sectionId) {
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    const activeLink = document.querySelector(`[data-section="${sectionId}"]`);
    if (activeLink) activeLink.classList.add('active');

    document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
    const activeSection = document.getElementById(sectionId);
    if (activeSection) activeSection.classList.add('active');

    document.getElementById('sectionTitle').textContent =
        sectionId.charAt(0).toUpperCase() + sectionId.slice(1);

    if (sectionId === 'guests') {
        loadGuestList();
    } else if (sectionId === 'overview') {
        initDashboard();
    } else if (sectionId === 'rooms') {
        loadFullRooms();
    } else if (sectionId === 'reservations') {
        loadFullReservations();
    } else if (sectionId === 'billing') {
        loadFullBilling();
    }
}

async function loadFullRooms() {
    const tbody = document.getElementById('roomListBody');
    if (!tbody) return;
    try {
        const resp = await fetch('api/rooms');
        const data = await resp.json();
        const list = data.data || [];
        tbody.innerHTML = list.map(r => `
            <tr>
                <td>${r.roomId}</td>
                <td><strong>${r.roomNumber}</strong></td>
                <td>${r.roomType}</td>
                <td>${r.ratePerNight.toLocaleString()}</td>
                <td><span class="status-badge ${r.available ? 'status-confirmed' : 'status-cancelled'}">${r.available ? 'Available' : 'Occupied'}</span></td>
                <td><button class="btn btn-sm btn-outline"><i class="fas fa-edit"></i></button></td>
            </tr>
        `).join('');
    } catch (err) { tbody.innerHTML = 'Error loading rooms'; }
}

async function loadFullReservations() {
    const tbody = document.getElementById('resListBody');
    if (!tbody) return;
    try {
        const resp = await fetch('api/reservations');
        const data = await resp.json();
        const list = data.data || [];
        tbody.innerHTML = list.map(r => `
            <tr>
                <td><strong>${r.reservationNumber}</strong></td>
                <td>${r.guest ? r.guest.firstName + ' ' + r.guest.lastName : 'ID: ' + r.guestId}</td>
                <td>${r.room ? 'Room ' + r.room.roomNumber : 'ID: ' + r.roomId}</td>
                <td>${r.checkInDate}</td>
                <td>${r.checkOutDate}</td>
                <td><span class="status-badge status-${r.status.toLowerCase()}">${r.status}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline" title="View Details"><i class="fas fa-eye"></i></button>
                    ${r.status === 'PENDING' ? `<button class="btn btn-sm btn-primary" onclick="updateResStatus(${r.reservationId},'confirm')">Confirm</button>` : ''}
                </td>
            </tr>
        `).join('');
    } catch (err) { tbody.innerHTML = 'Error loading reservations'; }
}

async function loadFullBilling() {
    const tbody = document.getElementById('billListBody');
    if (!tbody) return;
    try {
        const resp = await fetch('api/bills');
        const data = await resp.json();
        const list = data.data || [];
        tbody.innerHTML = list.map(b => `
            <tr>
                <td><strong>${b.billNumber}</strong></td>
                <td>
                    ${b.reservation ? b.reservation.reservationNumber : 'Res ID: ' + b.reservationId}
                    <div style="font-size: 0.7rem; color: var(--text-muted);">
                        ${b.reservation?.guest ? b.reservation.guest.firstName + ' ' + b.reservation.guest.lastName : ''}
                    </div>
                </td>
                <td>${b.totalAmount.toLocaleString()}</td>
                <td><span class="status-badge status-${b.paymentStatus.toLowerCase()}">${b.paymentStatus}</span></td>
                <td>${b.paymentMethod || '-'}</td>
                <td>${new Date(b.issuedAt).toLocaleString()}</td>
                <td><button class="btn btn-sm btn-outline"><i class="fas fa-print"></i></button></td>
            </tr>
        `).join('');
    } catch (err) { tbody.innerHTML = 'Error loading billing'; }
}

async function updateResStatus(id, action) {
    if (!confirm(`Are you sure you want to ${action} this reservation?`)) return;
    try {
        const resp = await fetch(`api/reservations/${id}/${action}`, { method: 'PUT' });
        if (resp.ok) { loadFullReservations(); updateCounts(); }
    } catch (err) { alert('Action failed'); }
}

async function logout() {
    await fetch('api/auth/logout');
    window.location.href = 'staff_login.html';
}
