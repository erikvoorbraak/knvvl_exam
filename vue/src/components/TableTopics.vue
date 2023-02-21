<template>
    <EasyDataTable
      :headers="headers"
      :items="items"
      :rows-per-page="20"
      :rows-items="[10, 20, 50, 100]">
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
        { text: "Vak", value: "label", sortable: true },
        { text: "# Vragen", value: "numQuestions"},
        { text: "N", value: "nQuestions", sortable: true },
      ];
      return {
        headers
      };
    },
    methods: {
      loadData: function() {
        axios
        .get('/api/topics')
        .then((response) => {
            this.items = response.data
          })
        }
    },
    data() {
      return {
        items: []
      }
    },
    mounted() {
        this.loadData();
    }
  });
  </script>