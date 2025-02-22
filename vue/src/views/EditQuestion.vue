<script setup>
import TableChanges from "../components/TableChanges.vue";
</script>
<template>
    <main>
        <h2 v-if="questionId">Vraag {{ questionId }}</h2>
        <h2 v-else-if="translatesId">Vertaling van {{ translatesId }}</h2>
        <h2 v-else>Nieuwe vraag</h2>

      <form v-on:submit.prevent="submit"> 
      <table>
        <tr><td>Vak</td><td>
            <select :disabled="loading" v-model="form.topicId">
                <option v-for="q in topics" :key="q.id" :value="q.id">{{ q.label }}</option>
            </select>
        </td></tr>
        <tr><td>Exameneis</td><td>
            <select :disabled="loading" v-model="form.requirementId">
                <option v-for="q in requirements" :key="q.id" :value="q.id">{{ q.subdomain }} {{ q.label }}</option>
            </select>
        </td></tr>
        <tr><td>Vraag:</td><td><textarea :disabled="loading" v-model="form.question" rows="4" cols="80"/></td><td><textarea v-if="form.question_original" disabled v-model="form.question_original" rows="4" cols="80"/></td></tr>
        <tr><td>A</td><td><textarea :disabled="loading" v-model="form.answerA" rows="2" cols="80"/></td><td><textarea v-if="form.answerA_original" disabled v-model="form.answerA_original" rows="2" cols="80"/></td></tr>
        <tr><td>B</td><td><textarea :disabled="loading" v-model="form.answerB" rows="2" cols="80"/></td><td><textarea v-if="form.answerB_original" disabled v-model="form.answerB_original" rows="2" cols="80"/></td></tr>
        <tr><td>C</td><td><textarea :disabled="loading" v-model="form.answerC" rows="2" cols="80"/></td><td><textarea v-if="form.answerC_original" disabled v-model="form.answerC_original" rows="2" cols="80"/></td></tr>
        <tr><td>D</td><td><textarea :disabled="loading" v-model="form.answerD" rows="2" cols="80"/></td><td><textarea v-if="form.answerD_original" disabled v-model="form.answerD_original" rows="2" cols="80"/></td></tr>
        <tr><td>Antwoord</td><td>
            <select :disabled="loading" v-model="form.answer">
                <option value="A">A</option>
                <option value="B">B</option>
                <option value="C">C</option>
                <option value="D">D</option>
            </select>
        </td></tr>
        <tr><td>Brevet 2</td><td>
            <select :disabled="loading" v-model="form.allowB2">
                <option value="true">Ja</option>
                <option value="false">Nee</option>
            </select>
        </td></tr>
        <tr><td>Brevet 3</td><td>
            <select :disabled="loading" v-model="form.allowB3">
                <option value="true">Ja</option>
                <option value="false">Nee</option>
            </select>
        </td></tr>
        <tr><td>Negeren</td><td>
            <select :disabled="loading" v-model="form.ignore">
                <option value="true">Ja</option>
                <option value="false">Nee</option>
            </select>
        </td></tr>
        <tr><td>Bespreken</td><td>
            <select :disabled="loading" v-model="form.discuss">
                <option value="true">Ja</option>
                <option value="false">Nee</option>
            </select>
        </td></tr>
        <tr><td>Opmerkingen</td><td><textarea :disabled="loading" v-model="form.remarks" rows="4" cols="80"/></td></tr>
        <tr><td>Examengroep</td><td><input :disabled="loading" v-model="form.examGroup"/></td></tr>
        <tr><td>Afbeelding</td><td>
            <select :disabled="loading" v-model="form.pictureId">
                <option v-for="q in pictures" :key="q.id" :value="q.id">{{ q.filename }}</option>
            </select>
        </td></tr>
        <tr><td>Taal</td><td>
            <select :disabled="loading" v-model="form.language">
                <option value="nl">nl</option>
                <option value="en">en</option>
            </select>
        </td></tr>
        <tr><td>Vertaling van</td><td><input :disabled="loading" type="number" v-model="form.translates"/></td></tr>
        <tr v-if="questionId"><td><a title="Percentage goed beantwoord na eventuele correctie, aantal examens waarin de vraag voorkwam, aantal keer beantwoord">Score</a></td>
            <td><input disabled v-model="form.scoreLabel"/></td></tr>
      </table>
      <button class="button is-primary">Submit</button>
      </form>
      <br/>
      <div v-if="questionId">
          <h2>Wijzigingen</h2>
          <TableChanges :questionId="questionId"/>
      </div>
    </main>
  </template>
  <script>
  import axios from 'axios'
  export default {
      data() {
          return {
              loading: false,
              questionId: this.$route.params.questionId,
              translatesId: this.$route.params.translatesId,
              topics: [],
              requirements: [],
              exams: [],
              pictures: [],
              form: {
                allowB2: "true",
                allowB3: "false",
                ignore: "true",
                discuss: "true"
              }
          }  
      },
      methods: {
          async submit() {
              try {
                if (this.questionId) {
                    await axios.post('/api/questions/' + this.questionId, this.form);
                }
                else {
                    await axios.post('/api/questions', this.form);
                }
                  this.$router.push('/questions');
              } catch (error) {
                  if (error.response) {
                    alert(error.response.data);
                  }
              }
          }
      },
      mounted() {
        document.title = this.questionId ? "Vraag bewerken" : "Vraag toevoegen";
        axios.get('/api/requirements').then((response) => { this.requirements = response.data });
        axios.get('/api/exams').then((response) => { this.exams = response.data });
        axios.get('/api/topics').then((response) => { this.topics = response.data });
        axios.get('/api/pictures').then((response) => { this.pictures = response.data });
        if (this.questionId) {
            this.loading = true;
            axios.get('/api/questions/' + this.questionId).then((response) => { 
                this.form = response.data;
                this.loading = false; });
        }
        if (this.translatesId) {
            this.loading = true;
            axios.get('/api/questions/' + this.translatesId + '/translated').then((response) => { 
                this.form = response.data
                this.loading = false; });
        }
    }
  }
  </script>