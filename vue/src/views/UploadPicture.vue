<template>
    <main>
    <p v-if="errorMessage" style="color: red;">{{ errorMessage }}</p>
    <form enctype="multipart/form-data" @submit.prevent="submitForm">
    <table>
      <tr><td>
        Filename (must be jpg)
      </td>
      <td>
        <input type="file" name="file" ref="fileInput" />
      </td></tr>
      <tr><td>
        <input type="submit" value="Upload" />
      </td></tr>
    </table>
		</form>
    </main>
  </template>
  <script>
    export default {
      data() {
          return {
              actionUrl: "/api/pictures" + (this.$route.params.pictureId ? "/" + this.$route.params.pictureId : ""),
              title: this.$route.params.pictureId ? "Bewerk afbeelding" : "Nieuwe afheelding",
              errorMessage: null
          }  
      },
      mounted() {
        document.title = this.title;
      },
      methods: {
        async submitForm() {
          this.errorMessage = null;
          const formData = new FormData();
          formData.append("file", this.$refs.fileInput.files[0]);
          try {
            const response = await fetch(this.actionUrl, {
              method: "POST",
              body: formData,
            });
            if (!response.ok) {
              const text = await response.text();
              this.errorMessage = `Upload mislukt (${response.status}): ${text}`;
              return;
            }
            this.$router.push("/pictures");
          } catch (err) {
            this.errorMessage = `Upload mislukt: ${err.message}`;
          }
        }
      }
    }
  </script>
