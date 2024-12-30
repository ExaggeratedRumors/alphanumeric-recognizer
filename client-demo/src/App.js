import React, { useState } from 'react';
import './App.css';

function App() {
  const [modelName, setModelName] = useState('');
  const [file, setFile] = useState(null);
  const [modelInfoResponse, setModelInfoResponse] = useState(null);
  const [dataInfoResponse, setDataInfoResponse] = useState(null);
  const [trainModelResponse, setTrainModelResponse] = useState(null);
  const [getStatusResponse, setGetStatusResponse] = useState(null);
  const [classifyImageReponse, setClassifyImageResponse] = useState(null);
  const [deleteResponse, setDeleteResponse] = useState(null);

  const [trainData, setTrainData] = useState({
    modelName: '',
    trainDataPath: '',
    trainLabelsPath: '',
    trainDataSize: 0,
    testDataPath: '',
    testLabelsPath: '',
    testDataSize: 0,
    epochs: 0,
    learningRate: 0,
    batchSize: 0,
    layers: [],
  });

  const [trainModelRequest, setTrainModelRequest] = useState(`{
    "modelName": "endpointTestModel",
    "trainDataPath": "emnist-balanced-train-images-idx3-ubyte",
    "trainLabelsPath": "emnist-balanced-train-labels-idx1-ubyte",
    "trainDataSize": 150,
    "testDataPath": "emnist-balanced-test-images-idx3-ubyte",
    "testLabelsPath": "emnist-balanced-test-labels-idx1-ubyte",
    "testDataSize": 10,
    "epochs": 1,
    "learningRate": 0.01,
    "batchSize": 10,
    "layers": [
      {
        "type": "Input",
        "height": 28,
        "width": 28,
        "channels": 1
      },
      {
        "type": "Conv",
        "filtersAmount": 8,
        "kernel": 3,
        "stride": 1,
        "padding": 0,
        "activation": "relu",
        "weightRange": 0.01
      },
      {
        "type": "MaxPool",
        "poolSize": 2,
        "stride": 2
      },
      {
        "type": "Flatten"
      },
      {
        "type": "Dense",
        "neurons": 10,
        "activation": "relu",
        "weightRange": 0.01
      },
      {
        "type": "Dropout",
        "rate": 0.1
      },
      {
        "type": "Dense",
        "neurons": 47,
        "activation": "softmax"
      }
    ]
  }`);

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };

  const handleTrainDataChange = (e) => {
    setTrainModelRequest(e.target.value);
  };

  const handleSubmit = async (url, method, data, setResponseFn) => {
    try {
      const response = await fetch(url, {
        method: method,
        headers: {
          'Content-Type': 'application/json',
        },
        body: data ? JSON.stringify(data) : null,
      });
      const result = await response.json();
      setResponseFn(result);
    } catch (error) {
      alert('Error: ' + error.message);
    }
  };

  const handleFileUpload = async () => {
    try {
      const response = await fetch('http://localhost:8080/classify', {
        method: 'POST',
        headers: {
          'Model-Name': modelName,
        },
        body: file,
      });
      const result = await response.json();
      setClassifyImageResponse(result);
    } catch (error) {
      alert('Error: ' + error.message);
    }
  };

  return (
      <div className="app">
        <h1>API Demo</h1>

        {/* GET: Models Info */}
        <section>
          <h2>Get Models Info</h2>
          <p>
            <strong>Method:</strong> GET <br />
            <strong>Endpoint:</strong> http://localhost:8080/models
          </p>
          <button onClick={() =>
              handleSubmit('http://localhost:8080/models', 'GET', null, setModelInfoResponse)
          }>Fetch Models</button>
          {modelInfoResponse && (
              <details>
                <summary>Click to see response</summary>
                <pre>{modelInfoResponse}</pre>
              </details>
          )}
        </section>

        {/* GET: Data Info */}
        <section>
          <h2>Get Data Info</h2>
          <p>
            <strong>Method:</strong> GET <br />
            <strong>Endpoint:</strong> http://localhost:8080/data
          </p>
          <button onClick={
            () => handleSubmit('http://localhost:8080/data', 'GET', null, setDataInfoResponse)
          }>Fetch Data</button>
          {dataInfoResponse && (
              <details>
                <summary>Click to see response</summary>
                <pre>{dataInfoResponse}</pre>
              </details>
          )}
        </section>

        {/* POST: Train Model */}
        <section>
          <h2>Train Model</h2>
          <p>
            <strong>Method:</strong> POST <br />
            <strong>Endpoint:</strong> http://localhost:8080/train
          </p>
          <textarea
              value={trainModelRequest}
              onChange={handleTrainDataChange}
              rows="10"
              placeholder="Enter JSON request body here"
          />
          <br />
          <button onClick={
            () => handleSubmit('http://localhost:8080/train', 'POST', JSON.parse(trainModelRequest), setTrainModelResponse)
          }>
            Train Model
          </button>
          {trainModelResponse && (
              <details>
                <summary>Click to see response</summary>
                <pre>{trainModelResponse}</pre>
              </details>
          )}
        </section>

        {/* GET: Status Info */}
        <section>
          <h2>Get Status Info</h2>
          <p>
            <strong>Method:</strong> GET <br />
            <strong>Endpoint:</strong> http://localhost:8080/status/{modelName}
          </p>
          <input
              type="text"
              placeholder="Enter Model Name"
              value={modelName}
              onChange={(e) => setModelName(e.target.value)}
          />
          <button onClick={
            () => handleSubmit(`http://localhost:8080/status/${modelName}`, 'GET', null, setGetStatusResponse)
          }>Fetch Status</button>
          {getStatusResponse && (
              <details>
                <summary>Click to see response</summary>
                <pre>{getStatusResponse}</pre>
              </details>
          )}
        </section>

        {/* POST: Classify Image */}
        <section>
          <h2>Classify Image</h2>
          <p>
            <strong>Method:</strong> POST <br/>
            <strong>Endpoint:</strong> http://localhost:8080/classify
          </p>
          <input
              type="text"
              placeholder="Enter Model Name"
              value={modelName}
              onChange={(e) => setModelName(e.target.value)}
          />
          <input type="file" accept="image/png" onChange={handleFileChange}/>
          <button onClick={handleFileUpload}>Upload and Classify</button>
          {classifyImageReponse && (
              <details>
                <summary>Click to see response</summary>
                <pre>{classifyImageReponse}</pre>
              </details>
          )}
        </section>

        {/* DELETE: Delete Model */}
        <section>
          <h2>Delete Model</h2>
          <p>
            <strong>Method:</strong> DELETE <br/>
            <strong>Endpoint:</strong> http://localhost:8080/model/{modelName}
          </p>
          <input
              type="text"
              placeholder="Enter Model Name"
              value={modelName}
              onChange={(e) => setModelName(e.target.value)}
          />
          <button onClick={
            () => handleSubmit(`http://localhost:8080/model/${modelName}`, 'DELETE', null, setDeleteResponse)
          }>Delete Model</button>
          {deleteResponse && (
              <details>
                <summary>Click to see response</summary>
                <pre>{deleteResponse}</pre>
              </details>
          )}
        </section>
      </div>
  );
}

export default App;