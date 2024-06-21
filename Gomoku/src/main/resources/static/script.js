var stompClient = null;
var isAnimating = false; // 用于跟踪动画状态

function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/users', function (message) {
            updateUsers(JSON.parse(message.body));
        });
        // 新增：订阅棋局更新
        stompClient.subscribe('/topic/game-progress', function (message) {
        showGameProgress(JSON.parse(message.body));
    });
    });
}

// 获取当前游戏状态
function showGameProgress(data) {
    updateBoard(data.board, data.removedPoints, data.randomRemovedPoints); // 更新棋盘
    updateUsers(data.users); // 更新用户列表
    if (data.poem) {
        displayPoem(data.poem); // 显示诗歌
    }
}

function updateUsers(users) {
    var usersList = document.getElementById('users');
    usersList.innerHTML = '';
    users.forEach(function(user) {
        var li = document.createElement('li');
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
            alert(data.winner + ' wins!');
            updateUsers(data.users);
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
                username.disabled = true;
                registerButton.disabled = true;
                updateUsers(data.users);
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

window.onload = function() {
    connect();
    createBoard();
    fetch('/game/users')
        .then(response => response.json())
        .then(data => {
            updateUsers(data); // 修改点5：更新用户列表，显示最新分数
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

// 定义一个方法，随机返回一个飘落动画类
function getRandomAnimationClass() {
    const animations = ['fall1', 'fall2', 'fall3', 'fall4'];
    return animations[Math.floor(Math.random() * animations.length)];
}

// 在指定时间后移除抖动效果并应用随机飘落效果
function applyShakeAndRandomFallAnimation(cell) {
    cell.classList.add('shake'); // 添加抖动动画效果

    setTimeout(() => {
        cell.classList.remove('shake');  // 延迟后移除抖动动画效果
        const animationClass = getRandomAnimationClass();
        cell.classList.add(animationClass); // 添加随机飘落效果

        // 动画结束后移除文本和样式
        setTimeout(() => {
            cell.innerText = '';
            cell.classList.remove(animationClass);
        }, 10000); // 动画持续3秒后清除内容和样式
    }, 1000); // 延迟1秒后移除抖动效果并应用飘落效果
}

// 页面加载时连接 WebSocket 并启动自动刷新
window.onload = function() {
    connect(); // 连接 WebSocket 服务器
    createBoard();
}