import math
import pygame

from game import GameType

class GameMenu:
    EXIT = "exit"
    WIN_1 = GameType.show_game_type(0)
    WIN_ALL = GameType.show_game_type(1)

    def __init__(self, game_type = None):
        pygame.init()
        self.width, self.height = 450, 400
        self.screen = pygame.display.set_mode((self.width, self.height))
        pygame.display.set_caption("Game Menu")
        self.font = pygame.font.Font(None, 36)
        self.running = True
        self.selected_option = None
        self.input_active = False
        self.game_type = game_type if game_type is not None else GameType()
        self.player_count = game_type.get_player_count()

        start_y = 120
        step_y = 50
        self.player_count_field_x = 100
        self.player_count_field_y = start_y
        
        self.kill_field_x = self.player_count_field_x + 150
        self.kill_field_y = start_y

        self.score_count_field_x = 100
        self.score_count_field_y = start_y + step_y
        self.option_score_rect = pygame.Rect(self.score_count_field_x, self.score_count_field_y, 100, 40)

        self.decrease_kill_time_field_x = self.player_count_field_x + 150
        self.decrease_kill_time_field_y = start_y + step_y
        self.option_fast_cat_rect = pygame.Rect(self.decrease_kill_time_field_x, self.decrease_kill_time_field_y, 180, 40)

        # Menu
        self.option1_rect = pygame.Rect(100, start_y + step_y * 2, 200, 40)
        self.option2_rect = pygame.Rect(100, start_y + step_y * 3, 200, 40)
        self.option_exit_rect = pygame.Rect(100, start_y + step_y * 4, 200, 40)

    def draw_menu(self):
        self.screen.fill((30, 30, 30))
        title_text = self.font.render("Select Game Mode", True, (255, 255, 255))
        self.screen.blit(title_text, (100, 50))
        
        # Draw player count input field
        count_text = self.font.render(f"Players: {self.player_count}", True, (255, 255, 255))
        self.screen.blit(count_text, (self.player_count_field_x, self.player_count_field_y))

        # Draw killer line input field
        killerr_text = self.font.render(f"Laser: {self.game_type.show_kill_type()}", True, (255, 255, 255))
        self.screen.blit(killerr_text, (self.kill_field_x, self.kill_field_y))

        # Draw score count input field
        score_text = self.font.render(f"Score: {self.game_type.get_winning_score()}", True, (255, 255, 255))
        self.screen.blit(score_text, self.option_score_rect.topleft)

        # Draw Fast Cat line input field
        fast_cat_text = self.font.render(f"Fast Cat: {self.game_type.get_decrease_kill_time()}", True, (255, 255, 255))
        self.screen.blit(fast_cat_text, self.option_fast_cat_rect.topleft)

        option1 = self.font.render("1. " + self.WIN_1, True, (200, 200, 200))
        option2 = self.font.render("2. " + self.WIN_ALL, True, (200, 200, 200))
        option_exit = self.font.render("3. " + self.EXIT.upper(), True, (100, 200, 200))
        
        self.screen.blit(option1, self.option1_rect.topleft)
        self.screen.blit(option2, self.option2_rect.topleft)
        self.screen.blit(option_exit, self.option_exit_rect.topleft)

        pygame.display.flip()

    def run(self):
        while self.running:
            self.draw_menu()
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    self.running = False
                    self.selected_option = self.EXIT
                elif event.type == pygame.KEYDOWN:
                    if event.key == pygame.K_1:
                        self.selected_option = self.WIN_1
                        self.game_type.set_type_1()
                        self.running = False
                    elif event.key == pygame.K_2:
                        self.selected_option = self.WIN_ALL
                        self.game_type.set_type_2()
                        self.running = False
                    elif event.key == pygame.K_3:
                        self.selected_option = self.EXIT
                        self.running = False
                    elif event.key == pygame.K_BACKSPACE:
                        self.player_count = self.game_type.set_player_count(self.player_count - 1)
                    elif event.key == pygame.K_SPACE:
                        self.player_count = self.game_type.set_player_count(self.player_count + 1)

                elif event.type == pygame.MOUSEBUTTONDOWN:
                    x, y = event.pos
                    # Player count
                    if self.player_count_field_x <= x <= self.player_count_field_x+120 and self.player_count_field_y <= y <= self.player_count_field_y + 30:
                        self.input_active = not self.input_active
                        if event.button == 1:# left button
                            self.player_count += 1
                        else:
                            self.player_count -= 1
                        self.player_count = self.game_type.set_player_count(self.player_count)
                    # Laser: False/True
                    if self.kill_field_x <= x <= self.kill_field_x+100 and self.kill_field_y <= y <= self.kill_field_y + 30:
                        self.game_type.up_kill_type()
                    # Score
                    if self.option_score_rect.collidepoint(event.pos):
                        self.input_active = not self.input_active
                        if event.button == 1:# left button
                            self.game_type.change_winning_score(self.game_type.get_winning_score()+1)
                        else:
                            self.game_type.change_winning_score(self.game_type.get_winning_score()-1)
                    # Fast Cat: False/True
                    if self.option_fast_cat_rect.collidepoint(event.pos):
                        self.game_type.change_decrease_kill_time()

                    if self.option1_rect.collidepoint(event.pos):
                        self.selected_option = self.WIN_1
                        self.game_type.set_type_1()
                        self.running = False
                    elif self.option2_rect.collidepoint(event.pos):
                        self.selected_option = self.WIN_ALL
                        self.game_type.set_type_2()
                        self.running = False
                    elif self.option_exit_rect.collidepoint(event.pos):
                        self.selected_option = self.EXIT
                        self.running = False

                elif event.type == pygame.TEXTINPUT and self.input_active:
                    if event.text.isdigit():
                        self.player_count = max(1, min(10, int(event.text)))
        
        pygame.quit()
        return self.selected_option