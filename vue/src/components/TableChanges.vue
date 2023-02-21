<template>
    <EasyDataTable
      :headers="headers"
      :items="items"
      :rows-per-page="100"
      :rows-items="[20, 100]">
      <template #item-changedAtEpoch="{ changedAt }">
        {{ changedAt }}
    </template>
    </EasyDataTable>
  </template>
  <script lang="ts">
  import { defineComponent } from "vue";
  import EasyDataTable from "vue3-easy-data-table";
  import type { Header } from "vue3-easy-data-table";
  import axios from 'axios'
  
  export default defineComponent({
    props: ['questionId'],
    setup() {
      const headers: Header[] = [
        { text: "Vraag", value: "questionId" },
        { text: "Door", value: "changedBy", sortable: true },
        { text: "Op", value: "changedAtEpoch", sortable: true, width: 100 },
        { text: "Veld", value: "field", sortable: true },
        { text: "Oude waarde", value: "oldValue", sortable: true, width: 200 },
        { text: "Nieuwe waarde", value: "newValue", sortable: true, width: 200 }
      ];
      return {
        headers
      };
    },
    methods: {
      loadQuestions: function() {
        axios
          .get('/api/questions/' + this.questionId + "/changes")
          .then((response) => {this.items = response.data})
        }
    },
    data() {
      return {
        questionId: this.$route.params.questionId,
        items: []
      }
    },
    mounted() {
        this.loadQuestions();
    }
  });
  </script>