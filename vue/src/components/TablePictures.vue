<template>
    <EasyDataTable
      :headers="headers"
      :items="items"
      :rows-per-page="10"
      :rows-items="[10, 20, 50, 100]">
        <template #item-id="{ id }">
          <a :style="{ cursor: 'pointer'}" @click="this.$router.push('/pictures/' + id)">{{ id }}</a>
       </template>
        <template #item-filename="{ filename, fileSize }">
          {{ filename }} ({{ fileSize }} bytes)
        </template>
        <template #item-fileSize="{ url }">
          <a target="_blank" :href="url"><img :src="url" height="50"></a>
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
        { text: "Filename", value: "filename", sortable: true },
        { text: "Preview", value: "fileSize", sortable: true },
        { text: "", value: "url" }
      ];
      return {
        headers
      };
    },
    methods: {
        loadRows: function() {
        axios
          .get('/api/pictures')
          .then((response) => {
              this.items = response.data
            })
        },
        deleteRow: function(url) {
          const me = this;
          if (confirm('Are you sure you want to delete this picture?')) {
              axios.delete(url).then(function() {me.loadRows()})
              .catch(function (error) {
                    if( error.response ){
                        alert(error.response.data);
                    }
                });
          }
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