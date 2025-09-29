// Inkball
const CELLSIZE = 32;
const TOPBAR = 64;
const WIDTH = 576;
const HEIGHT = 640;
const BOARD_WIDTH = WIDTH / CELLSIZE;
const BOARD_HEIGHT = (HEIGHT - TOPBAR) / CELLSIZE;

// Level layout
const levelLayout = [
  "4XXXXXXXXXXXXXXXX4",
  "X4X     H1     X4X", 
  "XX4X          X4XX",
  "X X4X        X4X X",
  "X  X4X      X4X  X",
  "X   X4X    X4X   X",
  "X    X      X    X",
  "X  B1S      S  H0X",
  "X                X",
  "X                X", 
  "X    S      S B2 X",
  "X    X      X    X",
  "X   X4X    X4X   X",
  "X  X4X      X4X  X",
  "X X4X        X4X X",
  "XX4X     H2   X4XX",
  "X4X            X4X",
  "4XXXXXXXXXXXXXXXX4"
];

// Game state
let balls = [];
let holes = [];
let spawners = [];
let drawnLines = [];
let currentLine = null;
let paused = false;
let score = 0;
let ballQueue = ["green", "grey", "grey", "blue", "yellow", "orange", "blue", "grey"];
let ballQueueIndex = 0;
let spawnTimer = 0;
let spawnInterval = 180; // 6 seconds at 30fps

// Ball class
class Ball {
  constructor(x, y, color) {
    this.x = x * CELLSIZE + CELLSIZE/2;
    this.y = y * CELLSIZE + TOPBAR + CELLSIZE/2;
    this.vx = random(-2, 2);
    this.vy = random(-2, 2);
    if (this.vx === 0) this.vx = 1;
    if (this.vy === 0) this.vy = 1;
    this.radius = 12;
    this.color = color;
    this.colorIndex = this.getColorIndex(color);
  }
  
  getColorIndex(color) {
    const colors = {"grey": 0, "orange": 1, "blue": 2, "green": 3, "yellow": 4};
    return colors[color] || 0;
  }
  
  update() {
    if (paused) return;
    
    this.x += this.vx;
    this.y += this.vy;
    
    // Bounce off walls
    if (this.x <= this.radius || this.x >= WIDTH - this.radius) {
      this.vx *= -1;
      this.x = constrain(this.x, this.radius, WIDTH - this.radius);
    }
    if (this.y <= TOPBAR + this.radius || this.y >= HEIGHT - this.radius) {
      this.vy *= -1;
      this.y = constrain(this.y, TOPBAR + this.radius, HEIGHT - this.radius);
    }
  }
  
  draw() {
    push();
    // Simple colored circles for balls
    let colors = {
      0: [128, 128, 128], // grey
      1: [255, 165, 0],   // orange
      2: [0, 0, 255],     // blue
      3: [0, 255, 0],     // green
      4: [255, 255, 0]    // yellow
    };
    let col = colors[this.colorIndex] || [128, 128, 128];
    fill(col[0], col[1], col[2]);
    stroke(0);
    strokeWeight(2);
    ellipse(this.x, this.y, this.radius * 2);
    pop();
  }
  
  checkHoleCollision(hole) {
    let dist = sqrt(pow(this.x - hole.x, 2) + pow(this.y - hole.y, 2));
    return dist < this.radius + 10;
  }
  
  reflectOffLine(line) {
    // Simple reflection - just reverse velocity
    this.vx *= -1;
    this.vy *= -1;
  }
}

// Hole class  
class Hole {
  constructor(x, y, colorIndex) {
    this.x = x * CELLSIZE + CELLSIZE/2;
    this.y = y * CELLSIZE + TOPBAR + CELLSIZE/2;
    this.colorIndex = colorIndex;
    this.size = CELLSIZE * 0.8;
  }
  
  draw() {
    push();
    // Simple colored squares for holes
    let colors = {
      0: [64, 64, 64],    // grey
      1: [200, 100, 0],   // orange
      2: [0, 0, 200],     // blue
      3: [0, 200, 0],     // green
      4: [200, 200, 0]    // yellow
    };
    let col = colors[this.colorIndex] || [64, 64, 64];
    fill(col[0], col[1], col[2]);
    stroke(0);
    strokeWeight(2);
    rectMode(CENTER);
    rect(this.x, this.y, this.size, this.size);
    pop();
  }
  
  attract(ball) {
    let dx = this.x - ball.x;
    let dy = this.y - ball.y;
    let dist = sqrt(dx * dx + dy * dy);
    
    if (dist < 60 && dist > 0) {
      let force = 0.05;
      ball.vx += (dx / dist) * force;
      ball.vy += (dy / dist) * force;
    }
  }
}

// Spawner class
class Spawner {
  constructor(x, y) {
    this.x = x * CELLSIZE + CELLSIZE/2;
    this.y = y * CELLSIZE + TOPBAR + CELLSIZE/2;
  }
  
  draw() {
    push();
    fill(255, 255, 0);
    stroke(0);
    strokeWeight(2);
    rectMode(CENTER);
    rect(this.x, this.y, CELLSIZE * 0.8, CELLSIZE * 0.8);
    pop();
  }
}

// Line drawing
class Line {
  constructor(x, y) {
    this.points = [{x: x, y: y}];
  }
  
  addPoint(x, y) {
    this.points.push({x: x, y: y});
  }
  
  draw() {
    if (this.points.length < 2) return;
    
    push();
    stroke(0);
    strokeWeight(8);
    noFill();
    beginShape();
    for (let point of this.points) {
      vertex(point.x, point.y);
    }
    endShape();
    pop();
  }
  
  checkCollision(ball) {
    for (let i = 0; i < this.points.length - 1; i++) {
      let p1 = this.points[i];
      let p2 = this.points[i + 1];
      let dist = this.distanceToLineSegment(ball.x, ball.y, p1.x, p1.y, p2.x, p2.y);
      if (dist < ball.radius + 4) {
        return true;
      }
    }
    return false;
  }
  
  checkMouseCollision(mx, my) {
    for (let i = 0; i < this.points.length - 1; i++) {
      let p1 = this.points[i];
      let p2 = this.points[i + 1];
      let dist = this.distanceToLineSegment(mx, my, p1.x, p1.y, p2.x, p2.y);
      if (dist < 10) {
        return true;
      }
    }
    return false;
  }
  
  distanceToLineSegment(px, py, x1, y1, x2, y2) {
    let A = px - x1;
    let B = py - y1;
    let C = x2 - x1;
    let D = y2 - y1;
    
    let dot = A * C + B * D;
    let lenSq = C * C + D * D;
    let param = (lenSq !== 0) ? dot / lenSq : -1;
    
    let xx, yy;
    if (param < 0) {
      xx = x1;
      yy = y1;
    } else if (param > 1) {
      xx = x2;
      yy = y2;
    } else {
      xx = x1 + param * C;
      yy = y1 + param * D;
    }
    
    let dx = px - xx;
    let dy = py - yy;
    return sqrt(dx * dx + dy * dy);
  }
}

function setup() {
  let canvas = createCanvas(WIDTH, HEIGHT);
  canvas.parent('sketch-holder');
  frameRate(30);
  
  initializeLevel();
}

function initializeLevel() {
  balls = [];
  holes = [];
  spawners = [];
  drawnLines = [];
  ballQueueIndex = 0;
  spawnTimer = 0;
  score = 0;
  
  // Parse level layout
  for (let y = 0; y < levelLayout.length; y++) {
    let row = levelLayout[y];
    for (let x = 0; x < row.length; x++) {
      let char = row[x];
      
      if (char === 'S') {
        spawners.push(new Spawner(x, y));
      } else if (char.startsWith('H')) {
        let holeType = parseInt(char[1]);
        holes.push(new Hole(x, y, holeType));
      } else if (char.startsWith('B')) {
        let ballType = parseInt(char[1]);
        // Start with a ball at this position
        let colors = ["grey", "orange", "blue", "green", "yellow"];
        balls.push(new Ball(x, y, colors[ballType] || "grey"));
      }
    }
  }
}

function draw() {
  background(220);
  
  // Draw grid background
  drawGrid();
  
  // Draw spawners
  for (let spawner of spawners) {
    spawner.draw();
  }
  
  // Draw holes and attract balls
  for (let hole of holes) {
    hole.draw();
    for (let ball of balls) {
      hole.attract(ball);
    }
  }
  
  // Update and draw balls
  let ballsToRemove = [];
  for (let i = 0; i < balls.length; i++) {
    let ball = balls[i];
    ball.update();
    ball.draw();
    
    // Check hole collisions
    for (let hole of holes) {
      if (ball.checkHoleCollision(hole)) {
        ballsToRemove.push(i);
        // Simple scoring
        if (ball.colorIndex === hole.colorIndex) {
          score += 25; // correct hole
        } else {
          score -= 5;  // wrong hole
        }
        break;
      }
    }
  }
  
  // Remove captured balls (in reverse order)
  for (let i = ballsToRemove.length - 1; i >= 0; i--) {
    balls.splice(ballsToRemove[i], 1);
  }
  
  // Draw lines and check ball collisions
  let linesToRemove = [];
  for (let i = 0; i < drawnLines.length; i++) {
    let line = drawnLines[i];
    line.draw();
    
    for (let ball of balls) {
      if (line.checkCollision(ball)) {
        ball.reflectOffLine(line);
        linesToRemove.push(i);
        break;
      }
    }
  }
  
  // Remove lines that were hit (in reverse order)
  for (let i = linesToRemove.length - 1; i >= 0; i--) {
    drawnLines.splice(linesToRemove[i], 1);
  }
  
  // Spawn new balls
  if (!paused) {
    spawnTimer++;
    if (spawnTimer >= spawnInterval && ballQueueIndex < ballQueue.length && spawners.length > 0) {
      let spawner = spawners[0]; // Use first spawner
      let color = ballQueue[ballQueueIndex];
      balls.push(new Ball(floor(spawner.x / CELLSIZE), floor((spawner.y - TOPBAR) / CELLSIZE), color));
      ballQueueIndex++;
      spawnTimer = 0;
    }
  }
  
  // Draw UI
  drawUI();
}

function drawGrid() {
  // Simple grid background
  push();
  fill(240);
  stroke(200);
  for (let y = 0; y < BOARD_HEIGHT; y++) {
    for (let x = 0; x < BOARD_WIDTH; x++) {
      rect(x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
    }
  }
  
  // Draw walls from level layout
  for (let y = 0; y < levelLayout.length; y++) {
    let row = levelLayout[y];
    for (let x = 0; x < row.length; x++) {
      let char = row[x];
      if (char === 'X' || char === '4') {
        fill(100);
        stroke(80);
        rect(x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
      }
    }
  }
  pop();
}

function drawUI() {
  // Top bar
  push();
  fill(50);
  noStroke();
  rect(0, 0, WIDTH, TOPBAR);
  
  fill(255);
  textAlign(LEFT, CENTER);
  textSize(20);
  text(`Score: ${score}`, 20, TOPBAR/2);
  
  textAlign(RIGHT, CENTER);
  text(`Balls: ${balls.length}`, WIDTH - 20, TOPBAR/2);
  
  if (paused) {
    textAlign(CENTER, CENTER);
    text("PAUSED", WIDTH/2, TOPBAR/2);
  }
  pop();
}

// Input handling
function keyPressed() {
  if (key === ' ') {
    paused = !paused;
  } else if (key === 'r' || key === 'R') {
    initializeLevel();
  }
}

function mousePressed() {
  if (mouseY < TOPBAR) return; // Don't draw in top bar
  
  if (mouseButton === RIGHT) {
    deleteLine();
  } else if (mouseButton === LEFT) {
    currentLine = new Line(mouseX, mouseY);
    drawnLines.push(currentLine);
  }
}

function mouseDragged() {
  if (mouseY < TOPBAR) return;
  
  if (mouseButton === LEFT && currentLine) {
    currentLine.addPoint(mouseX, mouseY);
  } else if (mouseButton === RIGHT) {
    deleteLine();
  }
}

function deleteLine() {
  for (let i = drawnLines.length - 1; i >= 0; i--) {
    if (drawnLines[i].checkMouseCollision(mouseX, mouseY)) {
      drawnLines.splice(i, 1);
      break;
    }
  }
}