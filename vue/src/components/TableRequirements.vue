<template>
    <EasyDataTable
      :headers="headers"
      :items="items"
      :rows-per-page="20"
      :rows-items="[20, 50, 100, 200]"
    />
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
        { text: "Exameneis", value: "label", sortable: true, width: 300 },
        { text: "Vak", value: "topic", sortable: true },
        { text: "Domain", value: "domain", sortable: true },
        { text: "Domaintitel", value: "domainTitle", sortable: true, width: 200 },
        { text: "Subdomein", value: "subdomain", sortable: true },
        { text: "N", value: "nQuestions", sortable: true },
      ];
      return {
        headers
      };
    },
    methods: {
        loadRequirements: function() {
        axios
        .get('/api/requirements')
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
        this.loadRequirements();
    }
  });
  </script>