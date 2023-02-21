<template>
  <h2>{{ examLabel }}</h2>
    <EasyDataTable
      :headers="headers"
      :items="items"
      :rows-per-page="100"
      :rows-items="[20, 100]">
      <template #item-id="{ id, examQuestionId }">
        <a :style="{ cursor: 'pointer'}" @click="this.$router.push('/examQuestion/' + examQuestionId)">{{ id }}</a>
      </template>
    </EasyDataTable>
  </template>
  <script lang="ts">
  import { defineComponent } from "vue";
  import EasyDataTable from "vue3-easy-data-table";
  import type { Header } from "vue3-easy-data-table";
  import axios from 'axios'
  
  export default defineComponent({
    setup() {
      const headers: Header[] = [
        { text: "ID", value: "id", sortable: true  },
        { text: "Vak", value: "topic", sortable: true },
        { text: "Exameneis", value: "requirement", sortable: true },
        { text: "Vraag", value: "question", sortable: true, width: 250 },
        { text: "A", value: "answerA", sortable: true, width: 200 },
        { text: "B", value: "answerB", sortable: true, width: 200 },
        { text: "C", value: "answerC", sortable: true, width: 200 },
        { text: "D", value: "answerD", sortable: true, width: 200 },
        { text: "Antw", value: "answer", sortable: true }
      ];
      return {
        headers
      };
    },
    methods: {
      loadQuestions: function() {
        document.title = "Examen " + this.examId;
        axios
          .get('/api/exams/' + this.examId + "/questions")
          .then((response) => {this.items = response.data})
        }
    },
    data() {
      return {
        examId: this.$route.params.examId,
        examLabel: "",
        items: []
      }
    },
    mounted() {
        this.loadQuestions();
        axios.get('/api/exams/' + this.examId).then((response) => { this.examLabel = response.data.label });
    }
  });
  </script>