<template>
    <RouterLink to="/newuser">Nieuwe gebruiker</RouterLink>
    <EasyDataTable
      :headers="headers"
      :items="items">
      <template #item-actions="item">
        <button @click="deleteUser(item)" class="delete-button">Delete</button>
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
        { text: "Username", value: "username", sortable: true },
        { text: "Email", value: "email", sortable: true },
        { text: "Actions", value: "actions" }
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
        },
      deleteUser: function(user: any) {
        if (confirm(`Are you sure you want to delete user "${user.username}"?`)) {
          axios
            .delete(`/api/users/${user.id}`)
            .then(() => {
              this.loadData();
            })
            .catch((error) => {
              let errorMessage = 'Error deleting user';
              
              if (error.response && error.response.data) {
                // Check if the response data is a string
                if (typeof error.response.data === 'string') {
                  errorMessage = error.response.data;
                } else if (error.response.data.message) {
                  errorMessage = error.response.data.message;
                } else {
                  // If it's an object, try to stringify it or show a generic message
                  errorMessage = 'Cannot delete user. The user may be referenced by other data.';
                }
              } else if (error.message) {
                errorMessage = error.message;
              }
              
              alert(errorMessage);
            });
        }
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
  <style scoped>
  .delete-button {
    background-color: #dc3545;
    color: white;
    border: none;
    padding: 5px 10px;
    cursor: pointer;
    border-radius: 3px;
  }
  .delete-button:hover {
    background-color: #c82333;
  }
  </style>