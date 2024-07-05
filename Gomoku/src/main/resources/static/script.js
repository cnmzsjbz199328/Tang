var stompClient = null;
var currentRoomName = null; // 当前房间名称
var subscriptions = {}; // 保存订阅的ID

window.onload = function() {
    connect();
    createBoard();
}

function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
    });
}

function subscribe(roomName) {
    if(!roomName){
    console.error('Room ID is empty');
    return;
    }

// 取消订阅之前的房间
    if (currentRoomName) {
        unsubscribe(currentRoomName);
    }
    currentRoomName = roomName;

    subscriptions['/topic/users/' + roomName] = stompClient.subscribe('/topic/users/' + roomName, function (message) {
        // 解析消息内容并调用 updateUsers 方法
                const data = JSON.parse(message.body);
                updateUsers(data);
    }).id;

    // 新增：订阅棋局更新
    subscriptions['/topic/game-progress/' + roomName] = stompClient.subscribe('/topic/game-progress/' + roomName, function (message) {
        // 解析消息内容并调用 showGameProgress 方法
                const data = JSON.parse(message.body);
                showGameProgress(data);
    }).id;
    console.log('Subscribed to room:' + roomName);
}

function unsubscribe(roomName) {
    if (stompClient && subscriptions['/topic/users/' + roomName]) {
            stompClient.unsubscribe(subscriptions['/topic/users/' + roomName]);
            delete subscriptions['/topic/users/' + roomName];
        }

    if (stompClient && subscriptions['/topic/game-progress/' + roomName]) {
            stompClient.unsubscribe(subscriptions['/topic/game-progress/' + roomName]);
            delete subscriptions['/topic/game-progress/' + roomName];
        }

    console.log('Unsubscribed from room:' + roomName);
}

// 获取当前游戏状态
function showGameProgress(data) {
    if (data.board) {
            updateBoard(data.board, data.removedPoints, data.randomRemovedPoints); // 更新棋盘
        } else {
            console.error('Invalid board data:', data.board);
        }

        if (data.users) {
            updateUsers(data.users); // 更新用户列表
        } else {
            console.error('Invalid users data:', data.users);
        }
    if (data.poem) {
        displayPoem(data.poem); // 显示诗歌
    }
    highlightCurrentPlayer(data.currentPlayer); // 高亮当前玩家
}

function updateUsers(data) {
// Extract users array from the received data
    var users;
console.log('Initial data received:', data);
        // 检查 data 是否是一个对象，并且包含 users 属性
        if (data && data.users && Array.isArray(data.users)) {
            users = data.users;
        } else if (Array.isArray(data)) {
            // 如果 data 是一个数组，直接赋值给 users
            users = data;
        } else {
            console.error('Invalid data format:', data);
            return;
        }

    console.log('Received users:', users);
    if (!Array.isArray(users)) {
            console.error('Expected an array of users, but got:', users);
            return;
    }
    var usersList = document.getElementById('users');
    usersList.innerHTML = '';

    users.forEach(function(user) {
        var li = document.createElement('li');
        li.id = 'user-' + user.name; // 为每个用户设置唯一的ID
        li.textContent = user.name + " - Score: " + user.score;
        usersList.appendChild(li);
    });
}

function makeMove(row, col) {
    fetch('/game/move', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include', // 发送请求时附带身份验证信息
        body: JSON.stringify({ row: row, col: col })
    })
    .then(response => {
                 if (!response.ok) {
                     console.error('Network response was not ok', response);
                     throw new Error('Network response was not ok');
                 }
                 const contentType = response.headers.get('content-type');
                 if (contentType && contentType.includes('application/json')) {
                     return response.json();
                 } else {
                     console.error('Received non-JSON response', response);
                     throw new Error('Received non-JSON response');
                 }
             })
    .then(data => {
        if (data.error) {
                    alert(data.error);
                    return;
                }

        console.log('Removed Points:', data.removedPoints);
        console.log('Random Removed Points:', data.randomRemovedPoints);

        updateBoard(data.board, data.removedPoints, data.randomRemovedPoints); // refresh the board

        if (data.winner) {
            updateUsers(data);
            blinkWinner(data.winner);
            if (data.poem) {
                displayPoem(data.poem);  // Display the poem
            }
        }
    })
    .catch(error => {
            console.error('Error:', error);
            alert('An error occurred: ' + error.message);
    });
}

function displayPoem(poem) {
    const gameTitleElement = document.getElementById('game-title');
    const poemSection = document.getElementById('poem-section');
    const poemElement = document.getElementById('poem');

    // Hide game title
    gameTitleElement.style.display = 'none';

    // Set poem text and show poem section
    poemElement.textContent = poem;
    poemSection.style.display = 'block';

    // Trigger a reflow to ensure the transition applies
    void poemSection.offsetWidth;

    // Apply fade-in effect
    poemSection.style.opacity = '1';
}

function createRoom() {
    const roomName = prompt("Enter room name:");
    const password = prompt("Enter password:");

    if (roomName && password) {
        fetch('/game/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `roomName=${encodeURIComponent(roomName)}&password=${encodeURIComponent(password)}`
        })
        .then(response => response.text())
        .then(roomName => {
            alert(`Room created successfully. Room ID: ${roomName}`);
            document.getElementById('room-name').value = roomName;
            subscribe(roomName); // 订阅新房间

            resetCurrentUserState(); // 清空当前用户的状态
        })
        .catch(error => {
            console.error('Error creating room:', error);
            alert('Failed to create room.');
        });
    }
}

function joinRoom() {
    const roomName = document.getElementById('room-name').value;
    const password = document.getElementById('room-password').value;

    if (roomName && password) {
            fetch('/game/join', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `roomName=${encodeURIComponent(roomName)}&password=${encodeURIComponent(password)}`,
                credentials: 'include'
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Invalid room ID or password');
                }
                return response.text();
            })
            .then(message => {
                alert(message);
                resetCurrentUserState(); // 清空当前用户的状态
                subscribe(roomName); // 订阅新房间的消息
                showGameProgress(roomName); // 获取当前游戏状态
            })
            .catch(error => {
                console.error('Error joining room:', error);
                alert('Failed to join room: ' + error.message);
            });
        } else {
            alert('Please enter both room ID and password.');
        }
    }

function updateBoard(board, removedPoints, randomRemovedPoints) {
    // 检查传入的board是否是一个有效的二维数组
    if (!Array.isArray(board) || !Array.isArray(board[0])) {
        console.error('Invalid board data:', board);
        return;
    }

    // 获取HTML中id为'board'的表格元素并清空其内容
    const table = document.getElementById('board');
    table.innerHTML = '';

    // 创建一个二维数组，用于存储被标记为移除的棋子
    const markedCells = Array(board.length).fill(null).map(() => Array(board[0].length).fill(null));

    // 将移除的棋子位置标记为'winner'
    if (removedPoints) {
        removedPoints.forEach(point => {
                markedCells[point.x][point.y] = 'winner';
                board[point.x][point.y] = point.player; // 添加棋子以便在棋盘中显示
        });
    }

    // 将随机移除的棋子位置标记为'random-removed'
    if (randomRemovedPoints) {
        randomRemovedPoints.forEach(point => {
            markedCells[point.x][point.y] = 'random-removed';
            board[point.x][point.y] = point.player; // 添加棋子以便在棋盘中显示
        });
    }

    // 遍历board数组，将每个棋子绘制到表格中
    for (let i = 0; i < board.length; i++) {
        let row = table.insertRow();  // 插入一行
        for (let j = 0; j < board[i].length; j++) {
            let cell = row.insertCell();  // 插入一个单元格
            cell.id = `cell-${i}-${j}`;  // 设置单元格的id
            cell.innerText = board[i][j] ? board[i][j] : '';  // 设置单元格的文本内容
            cell.onclick = () => makeMove(i, j);  // 设置单元格的点击事件

            // 如果该单元格被标记，则应用相应的特效类
            if (markedCells[i][j]) {
                cell.classList.add(markedCells[i][j]);
            }
        }
    }

    setTimeout(() => {
            const allRemovedPoints = [...(removedPoints || []), ...(randomRemovedPoints || [])];
            allRemovedPoints.forEach(point => {
                const cell = document.getElementById(`cell-${point.x}-${point.y}`);
                if (cell) {
                    const animationClass = applyShakeAndRandomFallAnimation(cell);
                    cell.classList.add(animationClass);  // 添加随机动画效果
                }
            });

            setTimeout(() => {
                    }, 10000); // 动画持续10秒后重置动画状态
                }, 1000); // 延迟1秒后执行动画效果和清除操作
            }


function registerPlayer() {
    // 检查会话中是否已经存在用户名
    fetch('/game/check-session', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.username) {
            alert("You have already registered as " + data.username);
        } else {
            // 如果会话中没有用户名，则执行注册
            var username = document.getElementById('username');
            var registerButton = document.querySelector('button[onclick="registerPlayer()"]');
            fetch('/game/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: 'username=' + encodeURIComponent(username.value)
            })
            .then(response => response.json())
            .then(data => {
                alert(data.message);
                //username.disabled = true;
                //registerButton.disabled = true;
                if (data.message === "Player registered successfully.") {
                            // Hide registration fields
                            username.style.display = 'none';
                            registerButton.style.display = 'none';
                        }
                updateUsers(data);
            })
            .catch(error => {
                console.error('Error during registration:', error);
            });
        }
    })
    .catch(error => {
        console.error('Error during session check:', error);
    });
}



function setStrategy() {
    var strategy = document.getElementById('strategy').value;
    fetch('/game/strategy', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: 'strategy=' + encodeURIComponent(strategy)
    });
}

function createBoard() {
    const table = document.getElementById('board');
    table.innerHTML = ''; // 清空表格
    for (let i = 0; i < 15; i++) {
        let row = table.insertRow();
        for (let j = 0; j < 15; j++) {
            let cell = row.insertCell();
            cell.onclick = () => makeMove(i, j);
        }
    }
}

function resetCurrentUserState() {
    // 清除当前用户名
    var username = document.getElementById('username');
    username.value = '';
    username.style.display = 'block'; // 确保注册字段重新显示

    // 重新启用注册按钮
    var registerButton = document.querySelector('button[onclick="registerPlayer()"]');
    registerButton.disabled = false;
    registerButton.style.display = 'block';

    // 清空用户列表
    var usersList = document.getElementById('users');
    usersList.innerHTML = '';

    // 其他需要清理的状态可以在这里添加
    console.log('Current user state reset.');
}

// 定义一个方法，随机返回一个飘落动画类
function getRandomAnimationClass() {
    const animations = ['fall1', 'fall2', 'fall3', 'fall4'];
    return animations[Math.floor(Math.random() * animations.length)];
}

// 在指定时间后移除抖动效果并应用随机飘落效果
function applyShakeAndRandomFallAnimation(cell) {
    cell.classList.add('shake'); // 添加抖动动画效果

    let delay = Math.random()*1000;

    setTimeout(() => {
        cell.classList.remove('shake');  // 延迟后移除抖动动画效果
        const animationClass = getRandomAnimationClass();
        cell.classList.add(animationClass); // 添加随机飘落效果

        // 动画结束后移除文本和样式
        setTimeout(() => {
            cell.innerText = '';
            cell.classList.remove(animationClass);
        }, 10000); // 动画持续3秒后清除内容和样式
    }, delay); // 延迟1秒后移除抖动效果并应用飘落效果
}

function typeWriterEffect(element, text, delay = 150) {
    let index = 0;
    element.textContent = '';  // 清空原内容
    const type = () => {
        if (index < text.length) {
            element.textContent += text.charAt(index);
            index++;
            if (index < text.length) {
                setTimeout(type, delay); // 递归调用以继续打印下一个字符
            }
        }
    };
    type();
}

function blinkWinner(winnerUsername) {
    const winnerElement = document.getElementById('user-' + winnerUsername);
    if (winnerElement) {
        // 随机选择一个颜文字
        const randomEmoticon = emoticons[Math.floor(Math.random() * emoticons.length)];

        // 设置span元素用来显示打字机效果
        const span = document.createElement('span');
        span.className = 'slide-right';
        winnerElement.innerHTML = '';  // 清空当前内容
        winnerElement.appendChild(span);

        typeWriterEffect(span, randomEmoticon, 150); // 使用随机颜文字和打字机效果

        setTimeout(() => {
            winnerElement.textContent = winnerUsername; // 10秒后恢复为原来的用户名
        }, 10000); // 根据动画持续时间自行调整
    } else {
        console.error('Winner element not found:', winnerUsername);
    }
}


const emoticons = [
    '૮₍ •́ ₃•̀₎ა',
    '૮₍ ˶•ᴗ•˶₎ა',
    '(｡♥‿♥｡)',
    '(*＾-＾*)',
    'ヾ(•ω•`)o',
    '(づ｡◕‿‿◕｡)づ',
    '☆*:.｡.o(≧▽≦)o.｡.:*☆',
    '(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧',
    '૮・ᴥ - ა',
    '૮₍ •̥𖥦•̥ ♡ ₎ა',
    '◖⚆ᴥ⚆◗',
    '૮・ᴥ・ა',
    '૮  ´͈ ᗜ `͈ ა♡',
    '٩(｡•́‿•̀｡)۶',
    '૮₍ ｡•.•｡₎ა',
    '٩(◕‿◕｡)۶',
];

function highlightCurrentPlayer(currentPlayer) {
    // Find all user elements and remove the blink class
    const userElements = document.querySelectorAll('#users li');
    userElements.forEach(el => el.classList.remove('blink'));

    // Find the current player element and add the blink class
    const currentPlayerElement = document.getElementById('user-' + currentPlayer);
    if (currentPlayerElement) {
        currentPlayerElement.classList.add('blink');

        // Remove the blink class after 10 seconds
        setTimeout(() => {
            currentPlayerElement.classList.remove('blink');
        }, 10000);
    }
}
