<template>
    <select name="topic" @change="filterByTopic">
        <option value="0">-- alle vakken --</option>
        <option v-for="q in topics" :key="q.id" :value="q.id">{{ q.label }}</option>
    </select>
    <select name="requirement" @change="filterByRequirement">
        <option value="0">-- alle eisen --</option>
        <option v-for="q in requirements" :key="q.id" :value="q.id">{{ q.subdomain }} {{ q.label }}</option>
    </select>
    <select name="exam" @change="filterByExam">
        <option value="0">-- alle examens --</option>
        <option v-for="q in exams" :key="q.id" :value="q.id">{{ q.label }}</option>
    </select>
    <input name="search" @keyup="filterBySearch"/>
</template>

<script>
import axios from 'axios'

export default {
    data() {
        return {
            topics: [],
            requirements: [],
            exams: []
        }
    },
    methods: {
        filterByTopic: function(e) {
            this.$emit("topicSelected", e.target.value);
        },
        filterByRequirement: function(e) {
            this.$emit("requirementSelected", e.target.value);
        },
        filterByExam: function(e) {
            this.$emit("examSelected", e.target.value);
        },
        filterBySearch: function(e) {
            this.$emit("searchSelected", e.target.value);
        },
    },
    mounted() {
        axios.get('/api/requirements').then((response) => { this.requirements = response.data });
        axios.get('/api/exams').then((response) => { this.exams = response.data });
        axios.get('/api/topics').then((response) => { this.topics = response.data });
    }
}
</script>