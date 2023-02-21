package org.knvvl.exam.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class QuestionTest
{
    @Test
    void setQuestion()
    {
        Question question = new Question();
        question.setQuestion("abc");
        assertEquals("Abc.", question.getQuestion());
        question.setQuestion("10!");
        assertEquals("10!", question.getQuestion());
    }
}