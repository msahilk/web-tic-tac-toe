
const wsUrl = 'http://localhost:8080/w24-csci2020u-final-project-1.0-SNAPSHOT/game/';


let currentRoomId = null;
let playerSymbol = null;
let webSocket = null;

document.getElementById('newGame').addEventListener('click', createNewGame);
document.getElementById('joinGame').addEventListener('click', joinGame);
document.querySelectorAll('.cell').forEach(cell => {
    cell.addEventListener('click', makeMove);
});



function joinGame() {
    const roomId = document.getElementById('roomIdInput').value.trim();
    if (roomId) {
        currentRoomId = roomId;
        playerSymbol = 'O';
        connectWebSocket(roomId);
        alert('Joined game in Room ID: ' + roomId);
    }
}

function connectWebSocket(roomId) {
    // if (webSocket) {
    //     webSocket.close();
    // }
    webSocket = new WebSocket(wsUrl + roomId);
    webSocket.onopen = function (e) {
        console.log('Connection established');
        console.log("Current socket state is " + webSocket.readyState);
    };
    webSocket.onmessage = function (event) {
        const data = JSON.parse(event.data);
        var stringied = JSON.stringify(data);
        console.log(stringied);

        updateBoard(data.board);
        if (data.winner) {
        alert('Game Over! Winner: ' + data.winner);
        webSocket.close();
        }
        };
        webSocket.onerror = function (event) {
            console.error('WebSocket error:', event);
        };
    }


function makeMove(event) {
    if (!currentRoomId || !playerSymbol) {
        alert('Please join a game first.');
        return;
    }
    const x = event.target.getAttribute('data-x');
    const y = event.target.getAttribute('data-y');

    let body = JSON.stringify({ x, y, player: playerSymbol });

    webSocket.send(body);
    console.log(body);
}

function updateBoard(board) {
    board.forEach((row, x) => {
        row.forEach((cell, y) => {
            document.querySelector(`.cell[data-x="${x}"][data-y="${y}"]`).textContent = cell;
        });
    });
}

async function fetchActiveRooms() {
    try {
        const response = await fetch('/w24-csci2020u-final-project-1.0-SNAPSHOT/api/game/activeRooms');
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const rooms = await response.json();
        const roomsList = document.getElementById('roomsList');
        roomsList.innerHTML = '';  // Clear any existing rooms

        rooms.forEach(roomId => {
            const listItem = document.createElement('li');
            listItem.textContent = roomId;
            roomsList.appendChild(listItem);
        });
    } catch (error) {
        console.error('Error fetching active rooms:', error);
    }
}

function createNewGame() {

    var roomId = document.getElementById("roomIdInput").value;
    currentRoomId = roomId;
    playerSymbol = 'X';
    alert('New game created! Room ID: ' + currentRoomId);

    playerSymbol = 'X';
    connectWebSocket(currentRoomId);


}

document.addEventListener('DOMContentLoaded', () => {
    fetchActiveRooms();
    setInterval(fetchActiveRooms, 5000);  // Update every 10 seconds
});