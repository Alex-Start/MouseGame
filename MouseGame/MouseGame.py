from game_menu import GameMenu
from game import GameType
from game import Game
from game_ui import GameUI

from logger import LoggerHelper
get_logger = LoggerHelper.get_logger("MouseGame")

if __name__ == "__main__":
    type_game = GameType()
    while True:
        menu = GameMenu(type_game)
        choice = menu.run()
        type_game = menu.game_type
        get_logger.info(f"Selected game mode: {type_game}")
        
        if choice == menu.EXIT:
            get_logger.info("Game Over!")
            break

        # Create and start the UI
        game_ui = GameUI(Game(type_game))  # Pass game instance to UI
        match type_game.get_kill_type():
            case 0:
                match choice:
                    case menu.WIN_1:
                        game_ui.run1()
                    case menu.WIN_ALL:
                        game_ui.run1()
                    case _:
                        get_logger.info("Invalid game mode: "+ choice)
                        get_logger.info("Game Over!")
                        break
            case 1:
                match choice:
                    case menu.WIN_1:
                        game_ui.run2()
                    case menu.WIN_ALL:
                        game_ui.run2()
                    case _:
                        get_logger.info("Invalid game mode: "+ choice)
                        get_logger.info("Game Over!")
                        break
            case 2:
                match choice:
                    case menu.WIN_1:
                        game_ui.run2()
                    case menu.WIN_ALL:
                        game_ui.run2()
                    case _:
                        get_logger.info("Invalid game mode: "+ choice)
                        get_logger.info("Game Over!")
                        break
