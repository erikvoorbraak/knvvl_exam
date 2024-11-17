<template>
    <main>
        <form v-on:submit.prevent="submit"> 
        {{ this.textKey }}<br/>
        <textarea v-model="form.label" cols="80" readonly/><br/>
        <textarea v-model="form.value" rows="20" cols="80"/><br/>
        <button class="button is-primary">Submit</button>
        </form>
    </main>
  </template>
  <script>
  import axios from 'axios'
  export default {
      data() {
          return {
              textKey: this.$route.params.textKey,
              form: {}
          }  
      },
      methods: {
          async submit() {
              try {
                var config = {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }
                await axios.post('/api/texts/' + this.textKey, this.form.value, config);
                this.$router.go(-1);
              } catch (error) {
                  if (error.response) {
                    alert(error.response.data);
                  }
              }
          }
      },
      mounted() {
        document.title = "Tekst bewerken";
        axios.get('/api/texts/' + this.textKey).then((response) => {
            this.form.label = response.data.label;
            this.form.value = response.data.value; });
    }
  }
  </script>