<template>
    <EasyDataTable
      :headers="headers"
      :items="items">
      <template #item-key="{ key }">
        <a :style="{ cursor: 'pointer'}" @click="this.$router.push('/texts/' + key)">{{ key }}</a>
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
        { text: "Key", value: "key", sortable: true  },
        { text: "Text", value: "label", sortable: true }
      ];
      return {
        headers
      };
    },
    methods: {
        loadData: function() {
            axios.get('/api/texts').then((response) => { this.items = response.data; })
        }
    },
    data() {
        return {
            items: []
      }
    },
    mounted() {
      document.title = "Settings";
      this.loadData();
    }
  });
  </script>