const fs = require('fs');
const { createObjectCsvWriter } = require('csv-writer');

// Define the path to the JSON file
const jsonFilePath = '../MA0RX-YMUMX-ZX4M4-USQ5H/MA0RX-YMUMX-ZX4M4-USQ5H.command.json';

// Read the JSON file
fs.readFile(jsonFilePath, 'utf8', (err, fileContents) => {
    if (err) {
        console.error('Error reading the JSON file:', err);
        return;
    }

    // Parse the JSON data
    const jsonData = JSON.parse(fileContents);

    // Assuming testID is the same for all entries in this batch and used to name the file
    const firstTestID = jsonData.data[0].testID;
    const csvFileName = `${firstTestID}-dataprepared.csv`;

    // Define the CSV writer with headers corresponding to the JSON data structure
    const csvWriter = createObjectCsvWriter({
        path: csvFileName,
        header: [
            {id: 'id', title: 'ID'},
            {id: 'testID', title: 'testID'},
            {id: 'screenshotPath', title: 'Screenshot Path'},
            {id: 'domPath', title: 'DOM Path'},
            {id: 'status', title: 'status'},
            {id: 'timestamp', title: 'timestamp'},
            {id: 'annotation', title: 'annotation'},
            {id: 'annotationLevel', title: 'annotationLevel'},
            {id: 'smartwaitRetry', title: 'smartwaitRetry'},
            {id: 'smartwaitRetryDelay', title: 'smartwaitRetryDelay'},
            {id: 'requestId', title: 'requestId'},
            {id: 'requestStartTime', title: 'requestStartTime'},
            {id: 'requestMethod', title: 'requestMethod'},
            {id: 'requestPath', title: 'requestPath'},
            {id: 'duration', title: 'duration'},
            {id: 'requestBody', title: 'requestBody'},
            {id: 'responseBody', title: 'responseBody'},
            {id: 'responseStatus', title: 'responseStatus'},
            {id: 'screenshotId', title: 'screenshotId'},
            {id: 'autoHeal', title: 'autoHeal'},
            {id: 'healedLocator', title: 'healedLocator'}
        ]
    });

    // Map JSON array to CSV rows
    const records = jsonData.data.map((item, index) => ({
        id: index,
        testID: item.testID,
        screenshotPath: item.Value.screenshotId ? `path/to/screenshots/${item.testID}-${index}.png` : '',
        domPath: item.healedLocator,
        status: item.status,
        timestamp: item.timestamp,
        annotation: item.annotation,
        annotationLevel: item.annotationLevel,
        smartwaitRetry: item.smartwaitRetry,
        smartwaitRetryDelay: item.smartwaitRetryDelay,
        requestId: item.Value.requestId,
        requestStartTime: item.Value.requestStartTime,
        requestMethod: item.Value.requestMethod,
        requestPath: item.Value.requestPath,
        duration: item.Value.duration,
        requestBody: item.Value.requestBody,
        responseBody: item.Value.responseBody,
        responseStatus: item.Value.responseStatus,
        screenshotId: item.Value.screenshotId,
        autoHeal: item.autoHeal,
        healedLocator: item.healedLocator
    }));

    // Write the CSV file
    csvWriter.writeRecords(records)
        .then(() => console.log(`The CSV file '${csvFileName}' was written successfully`));
});
