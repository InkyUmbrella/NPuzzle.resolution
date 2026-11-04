import pygame
import sys
import os

CELL_SIZE = 200
MARGIN = 40
TILE_COLOR = (97, 175, 239)
EMPTY_COLOR = (40, 44, 52)
TEXT_COLOR = (255, 255, 255)
BG_COLOR = (30, 30, 30)


def parse_file(filepath):
    frames = []
    current_frame = []

    with open(filepath, mode='r', encoding='utf-8') as f:
        lines = f.readlines()

    for line in lines:
        line = line.strip()
        if not line or line.startswith('+'):
            continue
        if line.startswith('|'):
            parts = line.split('|')[1:-1]
            row = []
            for p in parts:
                p = p.strip()
                if p == '#':
                    row.append(0)
                elif p.isdigit():
                    row.append(int(p))
            if row:
                current_frame.append(row)
        elif line.startswith('↓'):
            if current_frame and len(current_frame) == len(current_frame[0]):
                frames.append(current_frame)
                current_frame = []

    if current_frame and len(current_frame) == len(current_frame[0]):
        frames.append(current_frame)

    unique_frames = []
    for frame in frames:
        if not unique_frames or frame != unique_frames[-1]:
            unique_frames.append(frame)

    if not unique_frames:
        raise ValueError("解析不到棋盘")
    return unique_frames


def main():
    filepath = "astar-dell.txt"
    if not os.path.exists(filepath):
        print(f"❌ 找不到文件: {filepath}")
        sys.exit()

    try:
        frames = parse_file(filepath)
    except Exception as e:
        print(f"❌ 解析出错: {e}")
        sys.exit()

    N = len(frames[0])

    pygame.init()
    width = N * CELL_SIZE + MARGIN * 2
    height = N * CELL_SIZE + MARGIN * 2 + 80
    screen = pygame.display.set_mode((width, height))
    pygame.display.set_caption(f"N-Puzzle Visualizer - {N}x{N}")
    clock = pygame.time.Clock()

    # 🚨 这里改成了内置默认字体，完美避开底层报错
    font = pygame.font.Font(None, 60)
    info_font = pygame.font.Font(None, 30)

    current_frame = 0
    is_playing = False
    play_speed = 5
    timer = 0
    running = True

    while running:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_SPACE:
                    is_playing = not is_playing
                elif event.key == pygame.K_RIGHT:
                    is_playing = False
                    if current_frame < len(frames) - 1:
                        current_frame += 1
                elif event.key == pygame.K_LEFT:
                    is_playing = False
                    if current_frame > 0:
                        current_frame -= 1
                elif event.key == pygame.K_r:
                    current_frame = 0
                    is_playing = False
                elif event.key == pygame.K_UP:
                    play_speed = min(30, play_speed + 2)
                elif event.key == pygame.K_DOWN:
                    play_speed = max(1, play_speed - 2)

        if is_playing:
            now = pygame.time.get_ticks()
            if now - timer > (1000 / play_speed):
                timer = now
                if current_frame < len(frames) - 1:
                    current_frame += 1
                else:
                    is_playing = False

        screen.fill(BG_COLOR)
        state = frames[current_frame]

        for r in range(N):
            for c in range(N):
                val = state[r][c]
                x = MARGIN + c * CELL_SIZE
                y = MARGIN + r * CELL_SIZE
                rect = pygame.Rect(x, y, CELL_SIZE, CELL_SIZE)

                if val != 0:
                    pygame.draw.rect(screen, TILE_COLOR, rect.inflate(-4, -4), border_radius=8)
                    text_surf = font.render(str(val), True, TEXT_COLOR)
                    text_rect = text_surf.get_rect(center=rect.center)
                    screen.blit(text_surf, text_rect)
                else:
                    pygame.draw.rect(screen, EMPTY_COLOR, rect.inflate(-4, -4), border_radius=8)

        step_text = info_font.render(f"Step: {current_frame + 1} / {len(frames)} | Speed: {play_speed}", True,
                                     (200, 200, 200))
        screen.blit(step_text, (MARGIN, N * CELL_SIZE + MARGIN + 10))

        help_text = info_font.render("Space: Play/Pause | <-/->: Step | Up/Down: Speed | R: Reset", True,
                                     (150, 150, 150))
        screen.blit(help_text, (MARGIN, N * CELL_SIZE + MARGIN + 40))

        pygame.display.flip()
        clock.tick(60)

    pygame.quit()


if __name__ == "__main__":
    main()