<script setup>
import TableQuestions from "../components/TableQuestions.vue";
import QuestionFilters from "../components/QuestionFilters.vue";
document.title = "Vragen";
</script>
<template>
  <main>
    <RouterLink to="/newquestion">Nieuwe vraag</RouterLink><br/>
    <QuestionFilters 
      @topicSelected="id => topicId = id"
      @examSelected="id => examId = id"
      @requirementSelected="id => requirementId = id"
      @searchSelected="s => search = s" />
    <TableQuestions
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
          topicId: "0",
          requirementId: "0",
          examId: "0",
          search: ""
      }
  }
}
</script>