package com.mycompany.app.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.mycompany.app.achievement.AchievementTracker;
import com.mycompany.app.cli.HelpFormatter;
import com.mycompany.app.model.Card;
import com.mycompany.app.organizer.CardOrganizer;
import com.mycompany.app.organizer.RandomSorter;
import com.mycompany.app.organizer.RecentMistakesFirstSorter;
import com.mycompany.app.organizer.WorstFirstSorter;

public class FlashcardSession {
    private final List<Card> cards;
    private CardOrganizer organizer;
    private int requiredRepetitions;
    private final AchievementTracker tracker;
    private final Scanner scanner;
    private final Map<Card, Integer> correctAnswers;
    private final Map<Card, Integer> totalAttempts;
    private boolean isRunning = true;
    private boolean invertCards;

    public FlashcardSession(List<Card> cards, AchievementTracker tracker) {
        this.cards = cards;
        this.tracker = tracker;
        this.scanner = new Scanner(System.in);
        this.correctAnswers = new HashMap<>();
        this.totalAttempts = new HashMap<>();
        this.invertCards = false;

        for (Card card : cards) {
            correctAnswers.put(card, 0);
            totalAttempts.put(card, 0);
        }
    }

    public void start() {
        printWelcomeMessage();

        configureSession();
        
        while (isRunning) {
            resetSession();
            runFlashcardSession();
            
            if (isRunning) {
                if (promptForRestart()) {
                    if (promptForReconfiguration()) {
                        configureSession();
                    }
                } else {
                    isRunning = false;
                }
            }
        }
        
        tracker.displayAchievements();
        System.out.println("\nFlashcard ended");
    }

    private void printWelcomeMessage() {
        System.out.println("=================================================");
        System.out.println("Flashcard app started");
        System.out.println("=================================================");
        System.out.println("Set your flashcard session");
    }
    
    private void configureSession() {
        requiredRepetitions = promptForRepetitions();

        organizer = promptForOrderType();

        boolean newInvertSetting = promptForInvertCards();
        if (newInvertSetting != invertCards) {
            cards.forEach(Card::invert);
            invertCards = newInvertSetting;
        }
        
        System.out.println("\n=================================================");
        System.out.println("Set up done");
        System.out.println("Commands during the session:");
        System.out.println("  'skip' - to skip question");
        System.out.println("  'exit' - to exit the app");
        System.out.println("=================================================\n");
    }
    
    private boolean promptForRestart() {
        System.out.println("\nUmnuh session-d baisan kartuudaa dahin ajillah uu? (yes/no)");
        String restart = scanner.nextLine().trim().toLowerCase();
        return restart.equals("yes") || restart.equals("y");
    }
    
    private boolean promptForReconfiguration() {
        System.out.println("Songoltuudiig ahin oruulah uu (repetitions, order, inversion)? (yes/no)");
        String reconfigure = scanner.nextLine().trim().toLowerCase();
        return reconfigure.equals("yes") || reconfigure.equals("y");
    }
    
    private int promptForRepetitions() {
    while (true) {
        System.out.println("\nKart burt heden udaa zuw hariulah shaardlagatai ve? (1-10, or 'help'):");
        String input = scanner.nextLine().trim();
        
        if (input.equalsIgnoreCase("help")) {
            HelpFormatter.displayHelp();
            continue;
        }
        
        try {
            int repetitions = Integer.parseInt(input);
            if (repetitions >= 1 && repetitions <= 10) {
                return repetitions;
            }
        } catch (NumberFormatException e) {
            System.out.println("1ees 10 iin hoorond too eswel 'help' oruulna uu.");
        }
    }
}
    
    private CardOrganizer promptForOrderType() {
        List<String> validOptions = Arrays.asList("random", "worst-first", "recent-mistakes-first");
        
        System.out.println("\nChoose the order of cards:");
        System.out.println("1. random - Kartuud sanamsargui daraallaar garch irne");
        System.out.println("2. worst-first - Hamgiin ih aldsan kartuud ehleed garch irne");
        System.out.println("3. recent-mistakes-first - Suuld aldsan kartuud ehleed garch irne");
        
        while (true) {
            System.out.print("Songoltoo oruulna uu (1-3): ");
            String input = scanner.nextLine().trim();
            
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= 3) {
                    String orderType = validOptions.get(choice - 1);
                    return createOrganizer(orderType);
                }
            } catch (NumberFormatException e) {
                if (validOptions.contains(input.toLowerCase())) {
                    return createOrganizer(input.toLowerCase());
                }
            }
            
            System.out.println("1-ees 3-iin hoorond songono uu");
        }
    }
    
    private boolean promptForInvertCards() {
        while (true) {
            System.out.println("\nKartuudiin asuult hariultiin bairiig solih uu? (y/n):");
            String response = scanner.nextLine().trim().toLowerCase();
            
            if (response.equals("y") || response.equals("yes")) {
                return true;
            } else if (response.equals("n") || response.equals("no")) {
                return false;
            } else {
             System.out.println(" n(no) eswel y(yes) gej oruulna uu");
            }
        }
    }
    
    private CardOrganizer createOrganizer(String order) {
        switch (order) {
            case "worst-first":
                return new WorstFirstSorter();
            case "recent-mistakes-first":
                return new RecentMistakesFirstSorter();
            case "random":
            default:
                return new RandomSorter();
        }
    }
    
    private void resetSession() {
        for (Card card : cards) {
            correctAnswers.put(card, 0);
        }
    }

    private void runFlashcardSession() {
        boolean allCorrect = true;
        int round = 1;
        
        while (isRunning) {
            System.out.println("\n--- Round " + round + " ---");

            List<Card> organizedCards = organizer.organize(cards, correctAnswers, totalAttempts);
            
            boolean roundComplete = true;
            long roundStartTime = System.currentTimeMillis();
            int roundAnswers = 0;
            
            for (Card card : organizedCards) {
                if (!isRunning) {
                    return;
                }
                
                if (correctAnswers.get(card) < requiredRepetitions) {
                    roundComplete = false;
                    
                    boolean cardAnswered = processCard(card);
                    if (cardAnswered) {
                        roundAnswers++;
                    }
                    
                    if (!cardAnswered && !isRunning) {
                        return;
                    }
                }
            }
            
            if (roundComplete) {
                break;
            }
            
            if (roundAnswers > 0) {
                long roundEndTime = System.currentTimeMillis();
                double avgResponseTime = (roundEndTime - roundStartTime) / 1000.0 / roundAnswers;
                
                System.out.println("\nEne uyiin asuultuudad hariulsan dundaj hugatsaa : " + 
                        String.format("%.2f", avgResponseTime) + " seconds");
                
                if (avgResponseTime < 5.0) {
                    tracker.unlockAchievement("FAST");
                }
            }
            
            round++;
        }

        if (allCorrect && round > 1) {
            tracker.unlockAchievement("CORRECT");
        }
        
        System.out.println("\nKartuudad hariulj duuslaa");
    }
    
    private boolean processCard(Card card) {
        long startTime = System.currentTimeMillis();
        System.out.println("\nQuestion: " + card.getQuestion());
        System.out.print("Your answer (type 'skip' to skip, 'exit' to quit): ");
        String answer = scanner.nextLine().trim();
        
        if (answer.equalsIgnoreCase("exit")) {
            System.out.println("Exiting the session...");
            isRunning = false;
            return false;
        }
        
        if (answer.equalsIgnoreCase("skip")) {
            System.out.println("Skipping this question...");
            return false;
        }
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        totalAttempts.put(card, totalAttempts.get(card) + 1);
        
        if (card.checkAnswer(answer)) {
            System.out.println("Correct!");
            correctAnswers.put(card, correctAnswers.get(card) + 1);

            tracker.trackResponseTime(responseTime / 1000.0);

            if (totalAttempts.get(card) > 5) {
                tracker.unlockAchievement("REPEAT");
            }

            if (correctAnswers.get(card) >= 3) {
                tracker.unlockAchievement("CONFIDENT");
            }
        } else {
            System.out.println("Incorrect");
            System.out.println("The correct answer is: " + card.getAnswer());
        }
        
        return true;
    }
}