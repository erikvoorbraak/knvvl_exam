<script setup>
import TableQuestions from "../components/TableQuestions.vue";
import QuestionFilters from "../components/QuestionFilters.vue";

document.title = "Vragen";
</script>

<template>
  <main>
    <RouterLink to="/newquestion">Nieuwe vraag</RouterLink>
    &nbsp;<a href="/api/questions" target="_blank">Download as json</a>
    <br/>
    <QuestionFilters 
      @languageSelected="lng => language = lng"
      @topicSelected="id => topicId = id"
      @examSelected="id => examId = id"
      @requirementSelected="id => requirementId = id"
      @searchSelected="s => search = s" />
    <TableQuestions
     :language="language"
     :topicId="topicId"
     :requirementId="requirementId"
     :examId="examId"
     :search="search" />
  </main>
</template>
<script>
export default {
  data() {
      return {
          language: "",
          topicId: "0",
          requirementId: "0",
          examId: "0",
          search: ""
      }
  }
}
</script>