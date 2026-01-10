package org.knvvl.exam.services;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.knvvl.exam.entities.Picture;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Topic;

import com.google.common.base.Strings;

class ExamGeneratorHtml
{
    private static final String HTML = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>Oefenexamen</title>
            <style>
                body {
                    background-color: #f0f0f0;
                    font-family: Arial, sans-serif;
                    margin: 0;
                }
        
                .page-container {
                    max-width: 700px;
                    margin: 0 auto;
                }
        
                /* Sticky progress bar */
                .progress-wrapper {
                    position: sticky;
                    top: 0;
                    background-color: #f0f0f0;
                    padding: 15px 0;
                    z-index: 1000;
                }
        
                .progress-text {
                    text-align: right;
                    font-size: 14px;
                    margin-bottom: 6px;
                }
        
                .progress-container {
                    background-color: #ddd;
                    border-radius: 20px;
                    overflow: hidden;
                    height: 20px;
                }
        
                .progress-bar {
                    height: 100%;
                    width: 0%;
                    background-color: #4caf50;
                    transition: width 0.3s ease;
                }
        
                .exam-form {
                    padding: 20px 0 40px;
                }
        
                .question-container {
                    background-color: #ffffff;
                    padding: 20px;
                    border-radius: 6px;
                    margin-bottom: 20px;
                }
        
                .score-container {
                    background-color: #ffffff;
                    font-weight: bold;
                    padding: 20px;
                    border-radius: 6px;
                    margin-bottom: 20px;
                }
        
                .question-text {
                    font-weight: bold;
                    margin-bottom: 12px;
                }
        
                .answers label {
                    display: flex;
                    align-items: flex-start;
                    margin: 12px 0;
                    cursor: pointer;
                    font-size: 16px;
                }
        
                .answers input[type="radio"] {
                    transform: scale(1.5);
                    margin-right: 12px;
                    margin-top: 3px;
                }
        
                /* Submit button */
                .submit-container {
                    display: flex;
                    justify-content: center;
                    margin-top: 30px;
                }
        
                .submit-button {
                    background-color: #4caf50;
                    color: white;
                    font-size: 16px;
                    font-weight: bold;
                    padding: 14px 32px;
                    border: none;
                    border-radius: 30px;
                    cursor: pointer;
                    box-shadow: 0 4px 10px rgba(0,0,0,0.15);
                    transition: background-color 0.2s ease, transform 0.1s ease, box-shadow 0.1s ease;
                }
        
                .submit-button:hover {
                    background-color: #43a047;
                }
        
                .submit-button:active {
                    transform: translateY(1px);
                    box-shadow: 0 2px 6px rgba(0,0,0,0.15);
                }

                .submit-button:disabled {
                    background-color: #bdbdbd;
                    cursor: not-allowed;
                    box-shadow: none;
                }

                .submit-button:focus {
                    outline: none;
                    box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.4);
                }
            </style>
        </head>
        <body>
        
        <div class="page-container">
            {progressBar}
            <form method="POST" class="exam-form" id="examForm">
            <input type="hidden" id="questions" name="questions" value="{questionIds}">
            {score}
            {questions}
            {submit}
        </form>
    
        </div>
        <script>
            const form = document.getElementById("examForm");
            const progressBar = document.getElementById("progressBar");
            const progressText = document.getElementById("progressText");
            const submitButton = document.getElementById("submitButton");

            const questionNames = [...new Set(
                [...form.querySelectorAll('input[type="radio"]')]
                    .map(input => input.name)
            )];

            const totalQuestions = questionNames.length;

            function updateProgress() {
                let answered = 0;

                questionNames.forEach(name => {
                    if (form.querySelector(`input[name="${name}"]:checked`)) {
                        answered++;
                    }
                });

                const percent = Math.round((answered / totalQuestions) * 100);
                progressBar.style.width = percent + "%";
                progressText.textContent = percent + "% completed";

                // Enable submit only when all questions are answered
                submitButton.disabled = answered !== totalQuestions;
            }
            form.addEventListener("change", updateProgress);
        </script>
        
        </body>
        </html>""";

    private static final String PROGRESS = """
            <!-- Sticky progress bar -->
            <div class="progress-wrapper">
                <div class="progress-text" id="progressText">0%</div>
                <div class="progress-container">
                    <div class="progress-bar" id="progressBar"></div>
                </div>
            </div>
        """;

    private static final String SUBMIT = """
            <!-- Submit -->
            <div class="submit-container">
                <button type="submit" id="submitButton" class="submit-button" disabled>
                    Controleren
                </button>
            </div>
        """;

    private static final String QUESTION = """
        <div class="question-container">
            <div class="question-text">
            {number}. {question}
            </div>
            {picture}
            <div class="answers">
            {answers}
            </div>
        </div>
        """;

    private static final String PICTURE = """
        <img style="width:80%" src="/public/pictures/{picture}"/>
        """;

    private static final String ANSWER = """
        <label {style}>
            <input type="radio" name="{qid}" value="{abcd}" {checked}>{answer}
        </label>
        """;

    private static final String SCORE = """
        <div class="question-container">Jouw score: {score}%
        </div>
        """;

    private int qCounter = 1;
    private Topic currentTopic = null;
    private Map<String, String> questionsToAnswers = Collections.emptyMap();

    public String generateHtml(List<Question> questions)
    {
        String questionIds = questions.stream().map(Question::getId).map(String::valueOf).collect(joining(","));
        var questionsHtml = questions.stream().map(this::appendQuestion).collect(joining());
        return HTML
            .replace("{questionIds}", questionIds)
            .replace("{questions}", questionsHtml)
            .replace("{progressBar}", questionsToAnswers.isEmpty() ? PROGRESS : "")
            .replace("{score}", calculateScore(questions))
            .replace("{submit}", questionsToAnswers.isEmpty() ? SUBMIT : "");
    }

    private String calculateScore(List<Question> questions)
    {
        if (questions.isEmpty() || questionsToAnswers.isEmpty()) {
            return "";
        }
        long nCorrect = questions.stream()
            .filter(q -> Objects.equals(q.getAnswer(), questionsToAnswers.get(String.valueOf(q.getId()))))
            .count();
        int perc = (int) (100 * nCorrect / questions.size());
        return SCORE.replace("{score}", String.valueOf(perc));
    }

    public String checkPracticeExam(List<Question> questions, Map<String, String> questionsToAnswers)
    {
        this.questionsToAnswers = questionsToAnswers;
        return generateHtml(questions);
    }

    private String appendQuestion(Question question)
    {
        if (!Objects.equals(currentTopic, question.getTopic())) {
            currentTopic = question.getTopic();
            qCounter = 1;
        }
        String picture = ofNullable(question.getPicture()).map(Picture::getId)
            .map(id -> PICTURE.replace("{picture}", id.toString())).orElse("");
        String answers =
            getAnswer(question, "A", question.getAnswerA()) +
            getAnswer(question, "B", question.getAnswerB()) +
            getAnswer(question, "C", question.getAnswerC()) +
            getAnswer(question, "D", question.getAnswerD());
        String qnumber = String.valueOf(qCounter);
        qCounter++;
        return QUESTION
            .replace("{number}", qnumber)
            .replace("{question}", question.getQuestion())
            .replace("{picture}", picture)
            .replace("{answers}", answers);
    }

    private String getAnswer(Question question, String abcd, String answer)
    {
        String qid = question.getId().toString();
        String color = null;
        String givenAnswer = questionsToAnswers.get(qid);
        boolean thisAnswerIsCorrect = abcd.equals(question.getAnswer());
        boolean givenIsThisAnswer = abcd.equals(givenAnswer);
        if (!Strings.isNullOrEmpty(givenAnswer))
        {
            if (thisAnswerIsCorrect) {
                color = "green";
            }
            else if (givenIsThisAnswer) {
                color = "red";
            }
        }
        String style = color == null ? "" : "style=\"color:" + color + "\"";
        return ANSWER
            .replace("{style}", style)
            .replace("{qid}", qid)
            .replace("{abcd}", abcd)
            .replace("{checked}", givenIsThisAnswer ? "checked=\"checked\"" : "")
            .replace("{answer}", answer);
    }
}
