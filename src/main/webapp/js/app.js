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

        grid.innerHTML = rooms.map(room => {
            const activeRes = (window.allReservations || []).find(res =>
                res.roomId === room.roomId &&
                (res.status === 'CHECKED_IN' || res.status === 'CONFIRMED')
            );
            const statusText = room.available ? 'Available' : `Occupied by ${activeRes?.guest ? activeRes.guest.firstName : 'Guest'}`;

            return `
            <div class="room-node ${room.available ? 'available' : 'occupied'}" 
                 title="${room.roomNumber}: ${statusText} (${room.roomType})">
                ${room.roomNumber}
            </div>
            `;
        }).join('');
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

            // Switch to billing and ask to record payment
            showSection('billing');
            setTimeout(() => {
                if (confirm("Would you like to record a payment for this reservation now?")) {
                    fetch('api/bills').then(r => r.json()).then(data => {
                        const newBill = (data.data || []).find(b => b.reservationId === res.data.reservationId);
                        if (newBill) recordStaffPayment(newBill.billId);
                    });
                }
            }, 500);
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

// Global cache for filtering
window._allRoomsData = [];
window._allResData = [];

async function loadFullRooms() {
    const tbody = document.getElementById('roomListBody');
    if (!tbody) return;
    try {
        const [respRooms, respRes] = await Promise.all([
            fetch('api/rooms'),
            fetch('api/reservations')
        ]);
        const rooms = (await respRooms.json()).data || [];
        const reservations = (await respRes.json()).data || [];
        window.allReservations = reservations;
        window._allRoomsData = rooms;
        window._allResData = reservations;

        const avail = rooms.filter(r => r.available).length;
        const occup = rooms.filter(r => !r.available).length;
        const el1 = document.getElementById('roomCountAvailable');
        const el2 = document.getElementById('roomCountOccupied');
        if (el1) el1.textContent = avail;
        if (el2) el2.textContent = occup;

        renderRoomRows(rooms, reservations);
    } catch (err) { tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Error loading rooms</td></tr>'; }
}

function renderRoomRows(rooms, reservations) {
    const tbody = document.getElementById('roomListBody');
    if (!tbody) return;
    if (rooms.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:var(--text-muted);">No rooms found.</td></tr>';
        return;
    }
    tbody.innerHTML = rooms.map(r => {
        const activeRes = reservations.find(res =>
            res.roomId === r.roomId &&
            (res.status === 'CHECKED_IN' || res.status === 'CONFIRMED' || res.status === 'PENDING')
        );
        const guestInfo = activeRes
            ? `<div style="margin-top:4px;">
                <div style="font-weight:500; color:var(--primary);">${activeRes.guest ? activeRes.guest.firstName + ' ' + activeRes.guest.lastName : 'Guest'}</div>
                <div style="font-size:0.72rem; color:var(--text-muted);">${activeRes.reservationNumber} &bull; ${activeRes.checkInDate} → ${activeRes.checkOutDate}</div>
                <span style="font-size:0.7rem; padding:2px 8px; border-radius:10px; background:rgba(252,196,25,0.15); color:var(--primary);">${activeRes.status}</span>
               </div>`
            : '<span style="color:var(--text-muted); font-size:0.85rem;">—</span>';

        return `
        <tr>
            <td><strong>${r.roomNumber}</strong></td>
            <td>${r.roomType}</td>
            <td>${r.ratePerNight.toLocaleString()}</td>
            <td>
                <span class="status-badge ${r.available ? 'status-confirmed' : 'status-cancelled'}">
                    ${r.available ? 'Available' : 'Occupied'}
                </span>
            </td>
            <td>${guestInfo}</td>
            <td>
                ${r.available
                ? `<button class="btn btn-sm btn-primary" onclick="openBookingForRoom(${r.roomId})" style="padding:5px 10px; font-size:0.75rem;"><i class="fas fa-calendar-plus"></i> Book</button>`
                : `<button class="btn btn-sm btn-outline" style="padding:5px 10px; font-size:0.75rem; cursor:default;"><i class="fas fa-lock"></i> Reserved</button>`
            }
            </td>
        </tr>
        `;
    }).join('');
}

function filterRooms(type) {
    const rooms = window._allRoomsData;
    const res = window._allResData;
    if (type === 'available') renderRoomRows(rooms.filter(r => r.available), res);
    else if (type === 'occupied') renderRoomRows(rooms.filter(r => !r.available), res);
    else renderRoomRows(rooms, res);
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

// Global billing cache
window._allBillsData = [];

async function loadFullBilling() {
    const tbody = document.getElementById('billListBody');
    if (!tbody) return;
    try {
        const resp = await fetch('api/bills');
        const data = await resp.json();
        const list = data.data || [];
        window._allBillsData = list;

        // Update stat counters
        const pending = list.filter(b => b.paymentStatus === 'PENDING');
        const paid = list.filter(b => b.paymentStatus === 'PAID');
        const el1 = document.getElementById('billCountPending');
        const el2 = document.getElementById('billCountPaid');
        if (el1) el1.textContent = pending.length;
        if (el2) el2.textContent = paid.length;

        renderBillRows(list);
    } catch (err) { tbody.innerHTML = '<tr><td colspan="7">Error loading billing</td></tr>'; }
}

function renderBillRows(list) {
    const tbody = document.getElementById('billListBody');
    if (!tbody) return;
    if (list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:var(--text-muted);">No bills found.</td></tr>';
        return;
    }
    tbody.innerHTML = list.map(b => {
        const isPending = b.paymentStatus === 'PENDING';
        const statusClass = isPending ? 'status-pending' : 'status-confirmed';
        const statusLabel = isPending ? 'PAYABLE' : b.paymentStatus;
        const actionBtn = isPending
            ? `<button class="btn btn-primary" style="padding:5px 12px; font-size:0.75rem;" onclick="recordStaffPayment(${b.billId})"><i class="fas fa-cash-register"></i> Record Pay</button>`
            : `<span style="color:#4caf50; font-size:0.8rem;"><i class="fas fa-check-circle"></i> Settled</span>`;

        return `
        <tr>
            <td><strong>${b.billNumber}</strong></td>
            <td>
                <div style="font-weight:500;">${b.reservation?.guest ? b.reservation.guest.firstName + ' ' + b.reservation.guest.lastName : '—'}</div>
                <div style="font-size:0.72rem; color:var(--text-muted);">${b.reservation ? b.reservation.reservationNumber : 'Res ID: ' + b.reservationId}</div>
            </td>
            <td>${b.reservation?.room ? 'Room ' + b.reservation.room.roomNumber : '—'}</td>
            <td><strong>LKR ${b.totalAmount.toLocaleString()}</strong></td>
            <td><span class="status-badge ${statusClass}">${statusLabel}</span></td>
            <td>${b.paymentMethod || '—'}</td>
            <td>${actionBtn}</td>
        </tr>
        `;
    }).join('');
}

function filterBills(status) {
    const list = window._allBillsData;
    if (status === 'all') renderBillRows(list);
    else renderBillRows(list.filter(b => b.paymentStatus === status));
}

async function recordStaffPayment(billId) {
    const method = prompt("Enter Payment Method (CASH, CARD, BANK_TRANSFER):", "CASH");
    if (!method) return;

    try {
        const resp = await fetch(`api/bills/${billId}?method=${method}`, { method: 'PUT' });
        const res = await resp.json();
        if (res.success) {
            alert('Payment recorded successfully.');
            loadFullBilling();
            updateCounts();
        } else {
            alert('Error: ' + res.message);
        }
    } catch (err) { alert('Action failed'); }
}

function openBookingForRoom(roomId) {
    const section = document.querySelector('[data-section="overview"]');
    if (section) section.click();
    document.getElementById('btnQuickBooking').click();
    setTimeout(() => {
        document.getElementById('roomSelect').value = roomId;
    }, 500);
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
