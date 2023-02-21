<template>
    <main>
    <form method="POST" enctype="multipart/form-data" :action="actionUrl"> 
    <table>
      <tr><td>
        Filename (must be jpg)
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
    export default {
      data() {
          return {
              actionUrl: "/api/pictures" + (this.$route.params.pictureId ? "/" + this.$route.params.pictureId : ""),
              title: this.$route.params.pictureId ? "Bewerk afbeelding" : "Nieuwe afheelding"
          }  
      },
      mounted() {
        document.title = this.title;
    }
    }
  </script>