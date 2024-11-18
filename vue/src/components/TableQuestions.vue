<template>
  <EasyDataTable
    :headers="headers"
    :items="items"
    :rows-per-page="10"
    :rows-items="[10, 20, 50, 100]"
  >
    <template #item-id="{ id, translatable, translates }">
      <a :style="{ cursor: 'pointer' }"
        @click.exact="clickExact('/questions/' + id)"
        @click.ctrl="clickCtrl('/questions/' + id)">{{ id }}</a>
      <span v-if="translatable">
        <a :style="{ cursor: 'pointer' }" 
          @click.exact="clickExact('/translates/' + id)"
          @click.ctrl="clickCtrl('/translates/' + id)">
          <img src="/translate.png" width="16" height="16" title="Translate question to configured target language"/>
        </a>
      </span>
      <span v-if="translates">
        <a :style="{ cursor: 'pointer' }" 
          @click.exact="clickExact('/questions/' + translates)"
          @click.ctrl="clickCtrl('/questions/' + translates)">
          <img src="/translates.png" width="16" height="16" title="Go to original question that this is a translation of"/>
        </a>
      </span>
    </template>
    <template #item-tagsHtml="{ tagsHtml }">
      <span v-html="tagsHtml" />
    </template>
    <template #item-answerA="{ answerA, answer }">
      <span v-if="answer == 'A'"
        ><u>{{ answerA }}</u></span
      >
      <span v-else>{{ answerA }}</span>
    </template>
    <template #item-answerB="{ answerB, answer }">
      <span v-if="answer == 'B'"
        ><u>{{ answerB }}</u></span
      >
      <span v-else>{{ answerB }}</span>
    </template>
    <template #item-answerC="{ answerC, answer }">
      <span v-if="answer == 'C'"
        ><u>{{ answerC }}</u></span
      >
      <span v-else>{{ answerC }}</span>
    </template>
    <template #item-answerD="{ answerD, answer }">
      <span v-if="answer == 'D'"
        ><u>{{ answerD }}</u></span
      >
      <span v-else>{{ answerD }}</span>
    </template>
  </EasyDataTable>
</template>
<script lang="ts">
import { defineComponent } from "vue";
import EasyDataTable from "vue3-easy-data-table";
import type { Header, Item } from "vue3-easy-data-table";
import axios from "axios";

export default defineComponent({
  props: ["topicId", "requirementId", "examId", "search"],
  setup() {
    const headers: Header[] = [
      { text: "ID", value: "id", sortable: true },
      { text: "Vak", value: "topic", sortable: true },
      { text: "Exameneis", value: "requirement", sortable: true },
      { text: "Vraag", value: "question", sortable: true, width: 250 },
      { text: "A", value: "answerA", sortable: true, width: 200 },
      { text: "B", value: "answerB", sortable: true, width: 200 },
      { text: "C", value: "answerC", sortable: true, width: 200 },
      { text: "D", value: "answerD", sortable: true, width: 200 },
      { text: "Opmerkingen", value: "remarks", sortable: true },
      { text: "Tags", value: "tagsHtml" },
    ];
    return {
      headers,
    };
  },
  methods: {
    clickExact(location) {
      this.$router.push(location);
    },
    clickCtrl(location) {
      window.open(location, '_blank');
    },
    loadQuestions: function () {
      axios
        .get(
          "/api/questions?topic=" +
            this.topicId +
            "&requirement=" +
            this.requirementId +
            "&exam=" +
            this.examId +
            "&search=" +
            this.search
        )
        .then((response) => {
          this.items = response.data;
        });
    }
  },
  watch: {
    topicId() {
      this.loadQuestions();
    },
    requirementId() {
      this.loadQuestions();
    },
    examId() {
      this.loadQuestions();
    },
    search() {
      this.loadQuestions();
    },
  },
  data() {
    return {
      items: [],
    };
  },
  mounted() {
    this.loadQuestions();
  },
});
</script>
