<template>
  <EasyDataTable
    :headers="headers"
    :items="items"
    :rows-per-page="20"
    :rows-items="[20, 50, 100, 200]">

    <template #item-id="{ id }">
    <a :style="{ cursor: 'pointer' }"
      @click.exact="clickExact('/requirements/' + id)"
      @click.ctrl="clickCtrl('/requirements/' + id)">{{ id }}</a>
    </template>
    <template #item-delete="{ id }">
      <button @click="deleteRow(id)">Verwijder</button>
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
      { text: "Exameneis", value: "label", sortable: true, width: 300 },
      { text: "Vak", value: "topic", sortable: true },
      { text: "Domain", value: "domain", sortable: true },
      { text: "Domaintitel", value: "domainTitle", sortable: true, width: 200 },
      { text: "Subdomein", value: "subdomain", sortable: true },
      { text: "N", value: "nQuestions", sortable: true },
      { text: "", value: "delete" },
      ];
    return {
      headers
    };
  },
  methods: {
    clickExact(location) {
      this.$router.push(location);
    },
    clickCtrl(location) {
      window.open(location, '_blank');
    },
    loadRows: function() {
      axios
      .get('/api/requirements')
      .then((response) => {
          this.items = response.data
        })
    },
      deleteRow: function(id) {
        const me = this;
        if (confirm('Are you sure you want to delete this requirement?')) {
          try {
            axios.delete('/api/requirements/' + id).then(function() {me.loadRows()});
          }
          catch (error) {
            if (error.response) {
              alert(error.response.data);
            }
          }
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