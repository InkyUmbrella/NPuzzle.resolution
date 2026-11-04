import pygame
import sys
import os

# ================= 配置参数 =================
CELL_SIZE = 160
MARGIN = 30
TILE_COLOR = (97, 175, 239)
EMPTY_COLOR = (40, 44, 52)
TEXT_COLOR = (255, 255, 255)
BG_COLOR = (30, 30, 30)


def parse_file(filepath):
    puzzles = []  # 存放所有独立的题目
    current_puzzle = []
    current_frame = []

    with open(filepath, mode='r', encoding='utf-8') as f:
        lines = f.readlines()

    for line in lines:
        line = line.strip()

        # 核心切分点：遇到“启发函数”，说明上一道题彻底结束了
        if '启发函数' in line:
            if current_frame:
                current_puzzle.append(current_frame)
                current_frame = []
            if current_puzzle:
                # 去重当前题目的帧
                unique_frames = []
                for frame in current_puzzle:
                    if not unique_frames or frame != unique_frames[-1]:
                        unique_frames.append(frame)
                puzzles.append(unique_frames)
                current_puzzle = []
            continue

        if not line or line.startswith('+') or line.startswith('↓'):
            continue

        # 提取 4x4 数据
        if line.startswith('|') and line.endswith('|'):
            parts = line.split('|')[1:-1]
            row = []
            for p in parts:
                p = p.strip()
                if p == '#':
                    row.append(0)
                elif p.isdigit():
                    row.append(int(p))
            if len(row) == 4:
                current_frame.append(row)
                if len(current_frame) == 4:
                    current_puzzle.append(current_frame)
                    current_frame = []

    # 处理文件末尾如果还有没被“启发函数”切分的题目
    if current_frame:
        current_puzzle.append(current_frame)
    if current_puzzle:
        unique_frames = []
        for frame in current_puzzle:
            if not unique_frames or frame != unique_frames[-1]:
                unique_frames.append(frame)
        puzzles.append(unique_frames)

    if not puzzles:
        raise ValueError("未能解析出任何题目。")
    return puzzles


def main():
    filepath = "astar-dell-1.txt"

    if not os.path.exists(filepath):
        print(f"❌ 找不到文件: {filepath}")
        sys.exit()

    try:
        puzzles = parse_file(filepath)
    except Exception as e:
        print(f"❌ 解析出错: {e}")
        sys.exit()

    print(f"✅ 成功解析到 {len(puzzles)} 道 4x4 题目，准备渲染！")

    # 当前关卡索引
    puzzle_idx = 0
    frames = puzzles[puzzle_idx]
    N = 4

    pygame.init()
    width = N * CELL_SIZE + MARGIN * 2
    height = N * CELL_SIZE + MARGIN * 2 + 100
    screen = pygame.display.set_mode((width, height))
    pygame.display.set_caption(f"N-Puzzle Visualizer - 4x4 (Puzzle {puzzle_idx + 1}/{len(puzzles)})")
    clock = pygame.time.Clock()

    font = pygame.font.Font(None, 45)
    info_font = pygame.font.Font(None, 24)

    current_frame = 0
    is_playing = False
    play_speed = 8
    timer = 0
    running = True

    animating = False
    anim_progress = 0.0
    anim_speed = 0.15
    empty_prev = None

    def get_empty_pos(grid):
        for r in range(N):
            for c in range(N):
                if grid[r][c] == 0:
                    return (r, c)
        return (0, 0)

    def load_puzzle(idx):
        """切换关卡时重新初始化窗口和参数"""
        nonlocal frames, current_frame, empty_prev, screen, is_playing, animating
        frames = puzzles[idx]
        current_frame = 0
        is_playing = False
        animating = False
        empty_prev = get_empty_pos(frames[0])

        screen = pygame.display.set_mode((width, height))
        pygame.display.set_caption(f"N-Puzzle Visualizer - 4x4 (Puzzle {idx + 1}/{len(puzzles)})")

    load_puzzle(0)

    while running:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_SPACE:
                    is_playing = not is_playing
                elif event.key == pygame.K_RIGHT:
                    is_playing = False
                    if current_frame < len(frames) - 1 and not animating:
                        empty_prev = get_empty_pos(frames[current_frame])
                        current_frame += 1
                        animating = True
                        anim_progress = 0.0
                elif event.key == pygame.K_LEFT:
                    is_playing = False
                    if current_frame > 0 and not animating:
                        empty_prev = get_empty_pos(frames[current_frame - 1])
                        current_frame -= 1
                        animating = True
                        anim_progress = 0.0
                elif event.key == pygame.K_r:
                    current_frame = 0
                    is_playing = False
                    animating = False
                    empty_prev = get_empty_pos(frames[0])
                elif event.key == pygame.K_UP:
                    play_speed = min(30, play_speed + 2)
                elif event.key == pygame.K_DOWN:
                    play_speed = max(1, play_speed - 2)
                    # 🚨 新增：按 N 键切下一题，按 B 键切上一题
                elif event.key == pygame.K_n:
                    puzzle_idx = (puzzle_idx + 1) % len(puzzles)
                    load_puzzle(puzzle_idx)
                elif event.key == pygame.K_b:
                    puzzle_idx = (puzzle_idx - 1) % len(puzzles)
                    load_puzzle(puzzle_idx)

        if animating:
            anim_progress += anim_speed
            if anim_progress >= 1.0:
                anim_progress = 1.0
                animating = False

        if is_playing and not animating:
            now = pygame.time.get_ticks()
            if now - timer > (1000 / play_speed):
                timer = now
                if current_frame < len(frames) - 1:
                    empty_prev = get_empty_pos(frames[current_frame])
                    current_frame += 1
                    animating = True
                    anim_progress = 0.0
                else:
                    is_playing = False

        screen.fill(BG_COLOR)
        state = frames[current_frame]
        empty_curr = get_empty_pos(state)

        dr = empty_curr[0] - empty_prev[0]
        dc = empty_curr[1] - empty_prev[1]

        for r in range(N):
            for c in range(N):
                val = state[r][c]
                x = MARGIN + c * CELL_SIZE
                y = MARGIN + r * CELL_SIZE

                if animating and val != 0:
                    if (r, c) == empty_prev:
                        start_x = MARGIN + (c + dc) * CELL_SIZE
                        start_y = MARGIN + (r + dr) * CELL_SIZE
                        x = start_x + (x - start_x) * anim_progress
                        y = start_y + (y - start_y) * anim_progress

                rect = pygame.Rect(x, y, CELL_SIZE, CELL_SIZE)

                if val != 0:
                    pygame.draw.rect(screen, TILE_COLOR, rect.inflate(-4, -4), border_radius=8)
                    text_surf = font.render(str(val), True, TEXT_COLOR)
                    text_rect = text_surf.get_rect(center=rect.center)
                    screen.blit(text_surf, text_rect)
                else:
                    pygame.draw.rect(screen, EMPTY_COLOR, rect.inflate(-4, -4), border_radius=8)

        step_text = info_font.render(
            f"Step: {current_frame + 1} / {len(frames)} | Speed: {play_speed} | Puzzle: {puzzle_idx + 1}/{len(puzzles)}",
            True, (200, 200, 200))
        screen.blit(step_text, (MARGIN, N * CELL_SIZE + MARGIN + 15))

        help_text = info_font.render("Space: Play | Arrows: Step/Speed | N/B: Next/Prev Puzzle", True, (150, 150, 150))
        screen.blit(help_text, (MARGIN, N * CELL_SIZE + MARGIN + 45))

        pygame.display.flip()
        clock.tick(60)

    pygame.quit()


if __name__ == "__main__":
    main()