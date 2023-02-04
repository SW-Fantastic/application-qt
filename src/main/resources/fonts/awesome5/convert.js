const Files = require("fs");

let data = Files.readFileSync("font.json");
let jsonContent = new String(data);
let values = JSON.parse(jsonContent);

let iconNames = Object.getOwnPropertyNames(values);

let result = "";

for(let iconName of iconNames) {

    let template = 'awesomeMap.put("' + iconName + '\",\'' + values[iconName] + '\');';
    result = result + template + "\n";

}

Files.writeFileSync("awesome5.java",result);
console.log(result)