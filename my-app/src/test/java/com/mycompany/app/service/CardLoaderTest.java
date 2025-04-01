package com.mycompany.app.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mycompany.app.model.Card;

public class CardLoaderTest {

    @TempDir
    Path tempDir;
    
    private File testFile;
    
    @BeforeEach
    public void setUp() throws IOException {
        testFile = tempDir.resolve("test_cards.txt").toFile();
    }
    
    @Test
    public void testLoadCardsFromValidFile() throws IOException {
        List<String> lines = Arrays.asList(
            "Question 1",
            "Answer 1",
            "Question 2",
            "Answer 2",
            "Question 3",
            "Answer 3"
        );
        Files.write(testFile.toPath(), lines);
        
        List<Card> cards = CardLoader.loadCardsFromFile(testFile.getAbsolutePath());
        
        assertEquals(3, cards.size());
        assertEquals("Question 1", cards.get(0).getQuestion());
        assertEquals("Answer 1", cards.get(0).getAnswer());
        assertEquals("Question 2", cards.get(1).getQuestion());
        assertEquals("Answer 2", cards.get(1).getAnswer());
        assertEquals("Question 3", cards.get(2).getQuestion());
        assertEquals("Answer 3", cards.get(2).getAnswer());
    }
    
    @Test
    public void testLoadCardsFromFileWithOddLineCount() throws IOException {
 
        List<String> lines = Arrays.asList(
            "Question 1",
            "Answer 1",
            "Question 2",
            "Answer 2",
            "Question 3"  
        );
        Files.write(testFile.toPath(), lines);
        
        List<Card> cards = CardLoader.loadCardsFromFile(testFile.getAbsolutePath());
        
     
        assertEquals(2, cards.size());
        assertEquals("Question 1", cards.get(0).getQuestion());
        assertEquals("Answer 1", cards.get(0).getAnswer());
        assertEquals("Question 2", cards.get(1).getQuestion());
        assertEquals("Answer 2", cards.get(1).getAnswer());
    }
    
    @Test
    public void testLoadCardsFromEmptyFile() throws IOException {
      
        Files.write(testFile.toPath(), Arrays.asList());
        
        List<Card> cards = CardLoader.loadCardsFromFile(testFile.getAbsolutePath());
        
        assertTrue(cards.isEmpty());
    }
    
    @Test
    public void testLoadCardsFromNonexistentFile() {
        File nonExistentFile = tempDir.resolve("nonexistent.txt").toFile();
        
        Exception exception = assertThrows(IOException.class, () -> {
            CardLoader.loadCardsFromFile(nonExistentFile.getAbsolutePath());
        });
        
        assertTrue(exception.getMessage().contains("nonexistent.txt"));
    }
    
    @Test
    public void testLoadCardsWithSpecialCharacters() throws IOException {
    
        List<String> lines = Arrays.asList(
            "Үсэг?",
            "Letter",
            "Сайн байна уу?",
            "Hello",
            "Монгол",
            "Mongolia"
        );
        Files.write(testFile.toPath(), lines);
        
        List<Card> cards = CardLoader.loadCardsFromFile(testFile.getAbsolutePath());
        
        assertEquals(3, cards.size());
        assertEquals("Үсэг?", cards.get(0).getQuestion());
        assertEquals("Letter", cards.get(0).getAnswer());
        assertEquals("Сайн байна уу?", cards.get(1).getQuestion());
        assertEquals("Hello", cards.get(1).getAnswer());
        assertEquals("Монгол", cards.get(2).getQuestion());
        assertEquals("Mongolia", cards.get(2).getAnswer());
    }
}