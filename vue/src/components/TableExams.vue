<template>
    <EasyDataTable
      :headers="headers"
      :items="items"
      :rows-per-page="10"
      :rows-items="[10, 20, 50, 100]">
      <template #item-id="{ id }">
        <a :style="{ cursor: 'pointer'}" @click="this.$router.push('/exam/' + id)">{{ id }}</a><br/>
        <a :style="{ cursor: 'pointer'}" @click="this.$router.push('/examQuestions/' + id)">Vragen</a>
      </template>
      <template #item-fileSize="{ url, fileSize }">
        <a target="_blank" download="GeneratedExam" :href="url + '/generate'">Genereer</a>&nbsp;
        <a target="_blank" download="GeneratedExamWithId" :href="url + '/generate?withQuestionId=true'">+ID</a><br/>
        <span v-if="fileSize && fileSize != 0"><a target="_blank" download="UploadedExam" :href="url + '/pdf'">Download</a></span>
      </template>
      <template #item-url="{ url }">
        <button @click="deleteRow(url)">Verwijder</button>
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
        { text: "Naam", value: "label", sortable: true },
        { text: "Brevet", value: "certificate"},
        { text: "Pdf", value: "fileSize" },
        { text: "", value: "url" },
      ];
      return {
        headers
      };
    },
    methods: {
        loadRows: function() {
          axios
          .get('/api/exams')
          .then((response) => {
              this.items = response.data
            })
          },
        deleteRow: function(url) {
          const me = this;
          if (confirm('Are you sure you want to delete this exam?'))
              axios.delete(url).then(function() {me.loadRows()});
        }
    },
    data() {
      return {
        items: []
      }
    },
    mounted() {
        this.loadRows();
    }
  });
  </script>