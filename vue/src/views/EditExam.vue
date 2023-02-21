<template>
    <main>
        <h2>{{ form.label }}</h2>
        <form v-on:submit.prevent="submit"> 
            <table>
            <tr><td>Naam</td><td><input v-model="form.label"/></td></tr>
            <tr><td><button class="button is-primary">Change</button></td></tr>
            <tr><td>Vragen</td><td><textarea v-model="form.questions" readonly="true" rows="5" cols="120"/></td></tr>
            <tr><td>Antwoorden</td><td><textarea v-model="form.answers" readonly="true" rows="5" cols="120"/></td></tr>
            </table>
            
        </form>
        <br/>

        <h2>Upload PDF</h2>
        <form method="POST" enctype="multipart/form-data" :action="uploadUrl"> 
            <table>
            <tr><td>
                Filename (must be pdf)
            </td>
            <td>
                <input type="file" name="file" />
            </td></tr>
            <tr><td>
                <input type="submit" value="Upload" />
            </td></tr>
            </table>
		</form>
    </main>
  </template>
  <script>
  import axios from 'axios'
  export default {
      data() {
          return {
              examId: this.$route.params.examId,
              form: {},
              uploadUrl: "/api/exams/" + this.$route.params.examId + "/pdf"
          }  
      },
      methods: {
          async submit() {
              try {
                await axios.post('/api/exams/' + this.examId, this.form);
                this.$router.go(-1);
              } catch (error) {
                  if (error.response) {
                    alert(error.response.data);
                  }
              }
          }
      },
      mounted() {
        document.title = "Examen bewerken";
        axios.get('/api/exams/' + this.examId).then((response) => { this.form = response.data });
    }
  }
  </script>