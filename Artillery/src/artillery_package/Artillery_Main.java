package artillery_package;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Artillery_Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        playArtillery(input);
        printstring("Do you want to play again? (1 = yes/2 = no)");
        int playAgain = get_user_input(input, 1, 2);
        
        if (playAgain == 1) {
            playAgain = 0;
            playArtillery(input);
            /*
             * Ask the user to play again, if so, then run the method again
             */
        } else {
            printstring("Thank you for playing!");
        }

        input.close();
    }

    /*
     * declare the global variables here, some of these don't need to be global;
     * however, I chose to have these as global because I get an eagle eye view of objects
     * this class will be using without having any forseeable concerns for conflict with local variables
     */
    public static double tank_distance;
    public static int min_tank_distance = 20;
    public static int max_tank_distance = 200;
    public static int shot_distance;
    public static int hit_count;
    public static int player_perk;
    public static int difficulty_choice;
    public static int round_limit;
    public static int round_count;
    public static int explosion_radius = 10;
    public static int computer_difficulty_range = 0;
    public static int player_velocity;
    public static double player_angle;
    public static int player_score;
    public static int computer_score;
    public static int ScoreThreshold;
    public static double computer_theta;
    public static double computer_speed;

    public static void playArtillery(Scanner input) {
        printstring("Welcome to the Artillery game!");
        /*
         * Create the default values for a new game
         * select ammo, select turn count, select difficulty
         */
        player_score = 0;
        select_player_ammo(input);
        select_round_count(input);
        ScoreThreshold = round_limit;
        select_difficulty(input);
        generate_a_stage();

        while (!check_win_condition()) {
            player_turn(input);
            if (player_perk == 3 && !check_win_condition()) {
                printstring("You have the M240 B! Bonus turn!");
                player_turn(input);
            }
            computer_turn();
        }
        if (player_score >= ScoreThreshold) {
            printstring("Congratulations! You won the game!");
        } else {
            printstring("You lost the game. Better luck next time!");
        }
    }

    public static void select_player_ammo(Scanner input) {
        printstring("\nPlease select your perk type... for victory!!");

        printstring("\n( 1 ) Explosive rounds: Greater explosion radius, increasing your chances to land a shot.");
        /*
         * Increase explosion radius (as in tank_distance >20< instead of >10<)
         */
        timer(1);
        printstring("\n( 2 ) Spotter: Get slightly helpful feedback on where your shot lands");
        // return the distance of too short or too far on a miss
        timer(1);
        printstring("\n( 3 ) M240 B: Take 2 shots during your turn.");
        //take 2 turns!
        timer(1);
        player_perk = get_user_input(input, 1, 3);
        if (player_perk == 1) {
            explosion_radius = 20;
        }
    }

    public static void select_round_count(Scanner input) {
        printstring("\nEnter how many shots need to land for each player, the recommended amount is 3");
        printstring("but the maximum is 10");
        timer(1);
        round_limit = get_user_input(input, 1, 10);
        round_count = 0;
        ScoreThreshold = 0;
        /*
         * Since this method is only run when initializing the game, I reset the values
         * for
         * round count and score threshold here.
         */
    }

    public static void select_difficulty(Scanner input) {
        printstring("Now select your difficulty!");
        printstring("( 1 ) Easy - The enemy is taking shots in the dark. The battlefield is smaller.");
        // min 20, max 100
        timer(1);
        printstring("( 2 ) Average - The enemy is average... the stage is larger than Easy Mode...");
        timer(1);
        printstring("( 3 ) The enemy is a better shot and the stage is large!");
        // if difficulty is greater than 2 then the random distance between the tanks is
        // greater
        timer(1);
        difficulty_choice = get_user_input(input, 1, 3);

        switch (difficulty_choice) {
            case 1:
                computer_difficulty_range = 70;

                break;

            case 2:
                computer_difficulty_range = 50;

                break;

            case 3:
                computer_difficulty_range = 20;

                break;

            default:
                break;
        }
    }

    public static void player_turn(Scanner input) {
        printstring("It is your turn!");
        timer(1);
        printstring("Enter an angle for your attack:");
        player_angle = get_user_input(input, 0, 90);
        player_angle = Math.toRadians(player_angle);
        timer(1);
        printstring("Excellent, now enter the velocity for your attack:");
        player_velocity = get_user_input(input, 10, 9999);
        double distance = calculate_distance(player_velocity, player_angle);
        /* Needs to be switch because explosive rounds and then 
         * M240 B have different outcomes for distance
         */

        switch (player_perk) {
            case 1:
                if (distance < tank_distance - 20) {
                    printstring("Your shot was too short!");
                } else if (distance > tank_distance + 20) {
                    printstring("Your shot was too far!");
                } else {
                    printstring("Your shot landed! You gained a point.");
                    player_score++;
                    timer(1);
                }    
                break;
            case 2:
                if (distance < tank_distance - 10) {
                    printstring("Spotter: Your shot was " + (tank_distance + 10 - distance) + " meters too short!");
                } else if (distance > tank_distance + 10) {
                    printstring("Spotter: Your shot was " + (distance - tank_distance) + " meters too far!");
                } else {
                    printstring("Your shot landed! You gained a point.");
                    player_score++;
                    timer(1);
                }
                break;

            case 3:
                if (distance < tank_distance - 10) {
                    printstring("Your shot was too short!");
                } else if (distance > tank_distance + 10) {
                    printstring("Your shot was too far!");
                } else {
                    printstring("Your shot landed! You gained a point.");
                    player_score++;
                    timer(1);
                }
                /*
                 * Check the win condition here in case you won with the first shot but still need to take your
                 * second turn
                 */
                break;

            default:
                break;
        }
    }

    public static void computer_turn() {
        if (!check_win_condition()) {
            solve_for_theta();
            /*
             * Generate the angle
             */
            solve_for_computer_speed();
            /*
             * Generate the speed
             */
            double computer_shot_distance = calculate_distance(computer_speed, computer_theta);
            /* Calculate the distance, and create a rancom value between 0 and the
             * difficulty
             * The higher the difficulty, the higher the chances of a value
             */
            double distanceDifference = Math.abs((int) computer_shot_distance - tank_distance + generate_a_random_value(0, computer_difficulty_range));
            /*
             * Solve for the distance + or - 10 (using Math.abs), encapsulate the variable as
             * an int and check to see
             * if the shot landed
             */
            if (distanceDifference <= 10) {
                printstring("You got struck by the enemy!");
                computer_score++;
            } else {
                printstring("The enemy misses!");
            }
        }

    }

    public static void generate_a_stage() {
        switch (difficulty_choice) {
            // this switch will help generate the stage
            case 2:
                min_tank_distance = 30;
                max_tank_distance = 100;
                break;
            case 3:
                min_tank_distance = 50;
                max_tank_distance = 160;

            default:
                break;
        }
        tank_distance = (generate_a_random_value(min_tank_distance, max_tank_distance));
    }

    public static int get_user_input(Scanner input, int min, int max) {
        /*
         * Method for receiving user input, this prompt a player to enter a number,
         * which will return an integer that
         * contains the value that the user input
         */
        int val = min - 1;
        do {
            try { // Prompt the user to enter a value; if there is a failure, catch it within in
                  // the try block
                printstring("Enter a number between " + min + " and " + max + "\n");
                val = input.nextInt();
            } catch (Exception e) {
                /*
                 * e is the assignment to the exception, we could declare which exceptions we
                 * want to catch within
                 * this block; but because it is the scanner, I am comfortable with exception e.
                 */
                System.out.println("Invalid entry, try entering a number between " + min + " and " + max + "\n");
            } finally { // perform this action regardless whether an exception occurs
                input.nextLine();
            }
        } while (val < min || val > max);
        return val;
    }

    public static int generate_a_random_value(int min, int max) {
        /*
         * Generate a random value based on a minimum and a maximum,
         * the value must be a positive value between the two
         */
        Random rand = new Random();
        int random_value = Math.abs(rand.nextInt());
        random_value = (random_value % (max - min + 1)) + min;
        return random_value;
    }

    public static void solve_for_theta() {
        /*
         * Generate an angle between 10 and 90 degrees, then convert that angle to
         * radians to solve for speed
         * This is specific to the computer, otherwise the user will enter an angle
         */
        computer_theta = Math.toRadians(generate_a_random_value(10, 90));

    }

    public static void solve_for_computer_speed() {
        double denominator = Math.sin(computer_theta * 2);
        double numerator = tank_distance * 9.8;

        computer_speed = Math.sqrt(numerator / denominator);
    }

    public static boolean check_win_condition() {
        boolean victory = false;
        /*
         * Did I win???
        */
            if (player_score >= ScoreThreshold || computer_score >= ScoreThreshold) {
                victory = true;
            }
            return victory;
        }

    public static void timer(int second_counter) {
        /*
         * A timer used for pacing the way text is displayed.
         * This is only used for pacing, nothing else.
         */
        try {
            TimeUnit.MILLISECONDS.sleep(second_counter);
        } catch (InterruptedException e) {
        }
    }

    public static double calculate_distance(double velocity, double theta) {
        double distance = ((velocity * velocity) * (Math.sin(2 * theta))) / 9.8;
        // convert the speed and the distance to generate the shot (how long their
        // distance was)
        return distance;
    }

    public static void printstring(String text) {
        System.out.println();
        int size = text.length();
        for (int i = 0; i < size; i++) {
            System.out.print(text.charAt(i));
            timer(8);
        }
    }
}

