import pygame
import time

class GameUI:
    __mouse_file_pic = "mouse.jpg"
    __cat_file_pic = "cat.jpg"
    __x = 50 # border
    __text_width_x = 150 # e.g. 'Player 10 : 10'
    __mouse_start_x = __x + __text_width_x
    __mouse_step_x = 30
    __end_x = 50 # after finish line to show places
    __y = 50 # border
    __player_step_y = 40
    __end_y = 50 # bottom line control

    def __init__(self, game):
        self.game = game  # Store reference to Game object

        pygame.init()
        self.WIDTH, self.HEIGHT = self.__x + self.__mouse_start_x + self.game.winning_score * self.__mouse_step_x + self.__end_x + self.__x, self.__y + self.__player_step_y * len(self.game.players) + self.__end_y
        self.WHITE = (255, 255, 255)
        self.BLACK = (0, 0, 0)
        self.GREEN = (0, 255, 0)
        self.RED = (255, 0, 0)

        self.screen = pygame.display.set_mode((self.WIDTH, self.HEIGHT))
        pygame.display.set_caption("Mouse Game Scoreboard")
        self.font = pygame.font.Font(None, 36)

        self.mouse_image = pygame.image.load(GameUI.__mouse_file_pic)  # Load mouse icon
        self.mouse_image = pygame.transform.scale(self.mouse_image, (30, 30))  # Resize it

        self.mouse_killed_image = pygame.image.load(GameUI.__mouse_file_pic)  # Load mouse icon
        self.mouse_killed_image = pygame.transform.scale(self.mouse_killed_image, (30, 10))  # Resize it

        self.killer_line_image = pygame.image.load(GameUI.__cat_file_pic) # cat image
        self.killer_line_image = pygame.transform.scale(self.killer_line_image, (50, 50))  # Resize it

    def draw_scores(self):
        self.screen.fill(self.WHITE)  # Clear screen
        y = self.__y
        text_x = self.__x
        border_finish_line_y = 20 # for both side

        # Draw finish line
        finish_x = self.__mouse_start_x + self.game.winning_score * self.__mouse_step_x  # X position for the finish line
        pygame.draw.line(self.screen, self.GREEN, (finish_x, y - border_finish_line_y), (finish_x, y + self.__player_step_y * self.game.get_count_players()), 5)

        # draw time
        time_x = finish_x
        time_y = self.__y + self.__player_step_y * self.game.get_count_players()
        time_render = self.font.render(f"{self.game.get_duration_time()}", True, self.BLACK)
        self.screen.blit(time_render, (time_x, time_y))

        # draw statistic
        status_x = finish_x
        status_y = time_y + 20
        status_render = self.font.render(f"w: {self.game.get_count_winners()} k: {self.game.get_killed_players()}", True, self.BLACK)
        self.screen.blit(status_render, (status_x, status_y))

        for player in self.game.players:
            # Draw mouse icon (starting at __mouse_start_x, moving right as score increases)
            mouse_x = self.__mouse_start_x + (player.score * self.__mouse_step_x)  # Adjust distance
            if player.is_alive():
                self.screen.blit(self.mouse_image, (mouse_x, y - 10))
            else:
                self.screen.blit(self.mouse_killed_image, (mouse_x, y))
                pygame.draw.line(self.screen, self.BLACK, (text_x, y + 12), (text_x + self.__text_width_x, y + 12), 2)

            if player in self.game.winners.keys():
                color = self.RED
                # Draw win place
                place_number = self.game.winners.get(player)
                place = self.font.render(f"{place_number}", True, color)
                self.screen.blit(place, (mouse_x + self.__end_x, y))
            else:
                color = self.BLACK

            text = self.font.render(f"{player.name}: {player.score}", True, color)
            self.screen.blit(text, (text_x, y))
            
            y += self.__player_step_y

        # If a winner is found, display a message
        if self.game.get_count_winners() > 0:
            winner_text = self.font.render(", ".join(f"{player.name.strip() }" for player in self.game.get_winners()) + " WINS!", True, self.RED)
            self.screen.blit(winner_text, (self.__x, 10))

        pygame.display.flip()

    def draw_killer_line(self):        
        line_x = self.__mouse_start_x + (self.game.killer_line_number * self.__mouse_step_x)
        line_y = self.__y + self.__player_step_y * self.game.get_count_players()
        if self.game.killer_line_time < self.game.get_killer_line_time_max():
            # write time
            if self.game.killer_line_number > 1 and self.game.killer_line_time == self.game.get_killer_line_time_max() - 1:
                color = self.RED
            else:
                color = self.BLACK
            draw_killer = self.font.render(f"{self.game.killer_line_time}", True, color)
        else:
            if self.game.killer_line_number == 0: # skip the first line
                return
            # draw cat
            draw_killer = self.killer_line_image
            pygame.draw.line(self.screen, self.RED, (line_x + 10, self.__y - 20), (line_x + 10, line_y - 10), 5)
        
        self.screen.blit(draw_killer, (line_x, line_y))
        
        pygame.display.flip()

    def draw_game_over(self):
        x = self.__x
        y = self.__y + self.__player_step_y * len(self.game.players)
        text = self.font.render("Game Over!", True, self.RED)
        self.screen.blit(text, (x, y))
        pygame.display.flip()

    def run1(self):
        running = True
        while running:
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    running = False

            if self.game.is_finished():
                self.draw_game_over()
                time.sleep(0.1)  # Small delay to reduce CPU usage
            else:
                self.game.play_round()
                self.draw_scores()
                time.sleep(self.game.sleep)  # Add delay for better visualization

        pygame.quit()

    # with killer line
    def run2(self):
        running = True
        while running:
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    running = False

            if self.game.is_finished():
                self.draw_game_over()
                time.sleep(0.1)  # Small delay to reduce CPU usage
            else:
                self.game.up_killer_time()
                self.game.play_round()
                self.draw_scores()
                self.draw_killer_line()
                time.sleep(self.game.sleep/2)  # Add delay for better visualization
                
                self.game.refresh_player_for_killer_line()
                self.draw_scores()
                self.draw_killer_line()
                time.sleep(self.game.sleep/2)

        pygame.quit()

