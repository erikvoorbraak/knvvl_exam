<template>
    <EasyDataTable
      :headers="headers"
      :items="items"
      :rows-per-page="100"
      :rows-items="[20, 100]">
      <template #item-url="{ url, id }">
        <button @click="selectQuestion(url, id)">Select</button>
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
        document.title = "Selecteer vervangende vraag";
        const headers: Header[] = [
            { text: "", value: "url" },
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
            axios
            .get('/api/exams/altquestions/' + this.examQuestionId)
            .then((response) => {
                this.items = response.data
                })
        },
        async selectQuestion(url: string, id: string) {
            const config = { headers: {'Content-Type': 'text/plain'} };
            await axios.post(url, '' + id, config);
            this.$router.go(-1);
        }
    },
    data() {
        return {
            examQuestionId: this.$route.params.examQuestionId,
            items: []
        }
    },
    mounted() {
        this.loadQuestions();
    }
});
</script>
