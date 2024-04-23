function changePassword() {
    //TODO
    alert("nem");
}

function deleteRequest() {
    var txt = "Biztos törölni szeretnéd a fiókodat?\nA törlés végleges és a fiókhoz tartozó adatok is el fognak veszni!";
    if (confirm(txt)) {
        window.location.replace("deleteuser");
    }
}