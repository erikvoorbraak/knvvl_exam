<template>
    <RouterLink to="/newuser">Nieuwe gebruiker</RouterLink>
    <EasyDataTable
      :headers="headers"
      :items="items">
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
        { text: "Username", value: "username", sortable: true },
        { text: "Email", value: "email", sortable: true }
      ];
      return {
        headers
      };
    },
    methods: {
      loadData: function() {
        axios
        .get('/api/users')
        .then((response) => {
            this.items = response.data
          })
        }
    },
    data() {
        return { items: [] }
    },
    mounted() {
        document.title = "Users";
        this.loadData();
    }
  });
  </script>