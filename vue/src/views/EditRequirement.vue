<template>
    <main>
        <h2 v-if="requirementId">Exameneis {{ requirementId }}</h2>
        <h2 v-else>Nieuwe Exameneis</h2>
      <form v-on:submit.prevent="submit"> 
      <table>
        <tr><td>Vak</td><td>
            <select :disabled="loading" v-model="form.topicId">
                <option v-for="q in topics" :key="q.id" :value="q.id">{{ q.label }}</option>
            </select>
        </td></tr>
        <tr><td>Titel</td><td><input :disabled="loading" v-model="form.label"/></td></tr>
        <tr><td>Domein</td><td><input :disabled="loading" v-model="form.domain" type="number"/></td></tr>
        <tr><td>Domein titel</td><td><input :disabled="loading" v-model="form.domainTitle"/></td></tr>
        <tr><td>Subdomein</td><td><input :disabled="loading" v-model="form.subdomain"/></td></tr>
        <tr><td>B2 niveau</td><td><input :disabled="loading" v-model="form.levelB2"/></td></tr>
        <tr><td>B3 niveau</td><td><input :disabled="loading" v-model="form.levelB3"/></td></tr>
      </table>
      <button class="button is-primary">Submit</button>
      </form>
    </main>
  </template>
  <script>
  import axios from 'axios'
  export default {
      data() {
          return {
            loading: false,
            requirementId: this.$route.params.requirementId,
            topics: [],
            form: {
            }
          }  
      },
      methods: {
          async submit() {
              try {
                if (this.requirementId) {
                    await axios.post('/api/requirements/' + this.requirementId, this.form);
                }
                else {
                    await axios.post('/api/requirements', this.form);
                }
                  this.$router.push('/requirements');
              } catch (error) {
                  if (error.response) {
                    alert(error.response.data);
                  }
              }
          }
      },
      mounted() {
        document.title = this.requirementId ? "Exameneis bewerken" : "Exameneis toevoegen";
        axios.get('/api/topics').then((response) => { this.topics = response.data });
        if (this.requirementId) {
            this.loading = true;
            axios.get('/api/requirements/' + this.requirementId).then((response) => { 
                this.form = response.data;
                this.loading = false; });
        }
    }
  }
  </script>