import math
import random
import time

from logger import LoggerHelper
get_logger = LoggerHelper.get_logger("MouseGame")

class Player:
    __name_count_letters = 10
    def __init__(self, name):
        self.name = name + " "*(self.__name_count_letters-len(name))
        self.score = 0
        self.status = 1 # 1 - alive / 0 - die
    
    def update_score(self, points):
        if self.status == 0:
            return
        self.score = max(0, self.score + points)  # Ensure score is never negative
    
    def __str__(self):
        return f"{self.name}: {self.score} points"

    def kill(self, game_type):
        match game_type.get_kill_type():
            case 1:
                get_logger.info(f"Refresh Player: {self.name} ({self.score})=>0")
                self.score = 0
                
            case 2:
                get_logger.info(f"Kill Player: {self.name} ({self.score})=>0")
                self.status = 0

    def is_alive(self):
        return True if self.status == 1 else False

class Game:
    def __init__(self, game_type = None, sleep = 1):
        self.game_type = game_type if game_type is not None else GameType()

        self.players = [Player(f"Player {i+1}") for i in range(self.game_type.get_player_count())]
        # winners dict.: place: Player, "0":the last added place
        self.winners = {0:0}
        self.winning_score = self.game_type.get_winning_score()
        self.sleep = sleep

        self.killer_line_number = 0
        self.killer_line_time_max = 10 # each x time to fight
        self.killer_line_time = 0

        self.game_over = False

        self.start_time = time.time()  # Record start time

    
    def play_round(self):
        list_alive_players = [player for player in self.players if player.is_alive() and player.score < self.winning_score]
        possibility = ["one+1" for i in range(self.get_count_players())]#self.get_count_players() | len(list_alive_players)
        possibility.append("all+1")
        possibility.append("all-1")
        move_type = random.choice(possibility)
        
        if move_type == "one+1":
            player = random.choice(list_alive_players)#self.players
            self.update_score(player, 1)
            get_logger.info(f"{player.name} moves forward (+1)")
        elif move_type == "all+1":
            for player in self.players:
                self.update_score(player, 1)
            get_logger.info("All players move forward (+1)")
        elif move_type == "all-1":
            for player in self.players:
                self.update_score(player, -1)
            get_logger.info(f"All moves backward (-1)")
        
        self.show_scores()

        self.check_winner()
        
    def update_score(self, player, score):
        if player in self.winners.keys():
            return
        player.update_score(score)

    def show_scores(self):
        get_logger.info("Current Scores:")
        for player in self.players:
            get_logger.info(player)
        get_logger.info("-" * 30)
    
    def check_winner(self):
        winners_old = self.winners.keys()
        
        last_place = self.winners.get(0)
        for player in self.players:
            if player.score >= self.winning_score and not player in winners_old:
                last_place = self.winners.get(0) + 1
                self.winners[player] = last_place
                get_logger.info(f"🏆 {player.name} wins the game! {last_place} 🏆")
        
        self.winners[0] = last_place

    def up_killer_time(self):
        self.killer_line_time += 1
        if self.killer_line_time > self.get_killer_line_time_max():
            self.killer_line_number += 1
            if self.killer_line_number >= self.winning_score:
                self.killer_line_number = 1
            self.killer_line_time = 0
        get_logger.info(f"Killer line: {self.killer_line_time} {self.killer_line_number}")

    def refresh_player_for_killer_line(self):
        if not self.is_kill_time():
            return
        if self.killer_line_number == 0:
            return
        for player in self.players:
            if not player.is_alive():
                continue;
            if player.score == self.killer_line_number:
                player.kill(self.game_type)

    def is_kill_time(self):
        return self.killer_line_time == self.get_killer_line_time_max()

    # change max during game to show cat often
    def get_killer_line_time_max(self):
        if self.game_type.get_decrease_kill_time():
            start_run_faste = 5/6 # of whole distance
            step = max(1, self.winning_score * start_run_faste / self.killer_line_time_max) # each step line + 1
            change = int(max(1, self.killer_line_number / step))
            # change = self.killer_line_number
            return max(1, min(self.killer_line_time_max, self.killer_line_time_max - change + 1))
        else:
            return self.killer_line_time_max

    def get_winners(self):
        winners_list = list(self.winners.keys())
        winners_list.remove(0)
        return winners_list

    def get_count_winners(self):
        return len(self.winners.keys())-1

    def get_count_players(self):
        return len(self.players)

    def get_killed_players(self):
        return sum(not player.is_alive() for player in self.players)

    def is_finished(self):
        if self.game_over:
            return True

        self.game_over = self.check_is_finished()
        if self.game_over:
            get_logger.info("<<< Game Over! >>>")
            get_logger.info(f"duration: {self.calc_duration_time()}")
            return True

        return False

    def get_duration_time(self):
        if self.game_over:
            return self.formatted_duration_time
        return self.calc_duration_time()

    def calc_duration_time(self):
        end_time = time.time()  # Record end time
        duration = int(end_time - self.start_time)  # Convert to int (seconds)
        # Format to HH:MM:SS
        self.formatted_duration_time = time.strftime("%H:%M:%S", time.gmtime(duration))
        return self.formatted_duration_time

    def check_is_finished(self):
        get_logger.info(f"players: {self.get_count_players()} win: {self.get_count_winners()} killed: {self.get_killed_players()}")
        match self.game_type.get_game_type():
            case 0:
                if self.game_type.get_kill_type() == 2:
                    return self.get_count_winners() > 0 or self.get_killed_players() == self.get_count_players()
                else:
                    return self.get_count_winners() > 0
            case 1:
                if self.game_type.get_kill_type() == 2:
                    return self.get_count_winners() + self.get_killed_players() == self.get_count_players()
                else:
                    return self.get_count_winners() == self.get_count_players()
            case _:
                return False
    
    def start_for_console_only(self, count_winners):
        while True:
            self.play_round()
            if self.get_count_winners() >= count_winners:
                break
            time.sleep(self.sleep)  # Add delay for better visualization

class GameType:
    # game_type: 0 - win 1, 1 - win all
    # kill_type: 0 - Off, 1 - refressh -> 0, 2 - Kill
    __max_player_count = 10

    def __init__(self, player_count = 4, winning_score = 10, game_type = 0, kill_type = 0, decrease_kill_time = True):
        self.__player_count = self.set_player_count(player_count)
        self.__winning_score = winning_score
        self.__game_type = game_type
        self.__kill_type = kill_type
        self.__decrease_kill_time = decrease_kill_time

    def get_player_count(self):
        return self.__player_count

    def set_player_count(self, player_count):
        self.__player_count = max(1, min(self.__max_player_count, player_count))
        return self.__player_count

    def get_winning_score(self):
        return self.__winning_score

    def change_winning_score(self, score):
        self.__winning_score = max(1, score)

    def get_game_type(self):
        return self.__game_type

    def set_type_1(self):
        self.change_type(0)

    def set_type_2(self):
        self.change_type(1)

    def change_type(self, game_type):
        get_logger.info(f"Change game type: {game_type}")
        self.__game_type = game_type

    @staticmethod
    def show_game_type(game_type):
        match game_type:
            case 0:
                return "WIN 1"
            case 1:
                return "WIN ALL"
            case _:
                raise ValueError(f"Incorrect game type: {game_type}")

    def get_kill_type(self):
        return self.__kill_type

    def up_kill_type(self):
        self.__kill_type = self.__kill_type+1 if self.__kill_type < 2 else 0

    def show_kill_type(self):
        match self.__kill_type:
            case 0:
                return "Off"
            case 1:
                return "<<"
            case 2:
                return "Kill"
            case _:
                return "Off"

    def get_decrease_kill_time(self):
        return self.__decrease_kill_time

    def change_decrease_kill_time(self):
        self.__decrease_kill_time = not self.__decrease_kill_time

    def __str__(self):
        return f"game type: {GameType.show_game_type(self.__game_type)}, kill type: {self.show_kill_type()}, winning score: {self.__winning_score}"
