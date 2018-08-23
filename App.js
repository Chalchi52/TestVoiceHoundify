import React from 'react';
import ReactNative, { StyleSheet, Text, View, Button, DeviceEventEmitter } from 'react-native';
import Voice from 'react-native-voice';

let ReactHoundifyTest = ReactNative.NativeModules.ReactHoundifyTest;

export default class App extends React.Component {
  constructor(props) {
    super(props);
    console.log(ReactHoundifyTest);

    this.state = {
      result: 'No result recovered',
      status: 'Not recording'
    };

    DeviceEventEmitter.addListener('onHoundifyResponse', this.onHoundifyResponse);
    DeviceEventEmitter.addListener('onStartRecording', this.onStartRecording);
    DeviceEventEmitter.addListener('onStopRecording', this.onStopRecording);

    Voice.onSpeechStart = this.onVoiceToTextStart.bind(this);
    Voice.onSpeechEnd = this.onVoiceToTextEnd.bind(this);
    Voice.onSpeechResults = this.onVoiceToTextResult.bind(this);
    DeviceEventEmitter.addListener('onHoundifyTextResponse', this.onHoundifyTextResponse);
  }

  /*************************************************************************************************
  *                                         HOUNDIFY
  * ***********************************************************************************************/
  _onStartRecording() {
    ReactHoundifyTest.StartRecording();
  }

  _onStopRecording() {
    ReactHoundifyTest.StopRecording();
  }

  onHoundifyResponse = (response) => {
    let writtenResponse = JSON.parse(response).AllResults[0].WrittenResponse;
    console.log(JSON.parse(response));
    this.setState({
      result: writtenResponse
    });
  };

  onStartRecording = (response) => {
    this.setState({
      status: 'Recording'
    });
  };

  onStopRecording = (response) => {
    this.setState({
      status: 'Not recording'
    });
  };


  /*************************************************************************************************
  *                                     react-native-voice
  * ***********************************************************************************************/
  _voiceToTextStart() {
    console.log(Voice);
    Voice.start('en-US');
  }

  _voiceToTextStop() {
    Voice.stop();
  }

  onVoiceToTextStart(event) {
    this.setState({
      status: 'Listening'
    });
  }

  onVoiceToTextEnd(event) {
    this.setState({
      status: 'Not listening'
    });
  }

  onVoiceToTextResult(event) {
    if(!event || !event.value || event.value.length === 0) {
      return;
    }

    this.setState({
      result: event.value[0]
    });

    console.log(`/****************${event.value[0]}`);
    ReactHoundifyTest.SearchText(event.value[0]);
  }

  onHoundifyTextResponse = (response) => {
    let writtenResponse = JSON.parse(response).AllResults[0].WrittenResponse;
    console.log(`/////////////////////////////${writtenResponse}`);

    this.setState({
      result: writtenResponse
    });
  }

  /*************************************************************************************************
  *                                            render
  * ***********************************************************************************************/
  render() {

    return (
      <View style={styles.container}>
        <Text>{this.state.result}</Text>

        <Text> Status: {this.state.status}</Text>

        <View style={styles.row}>
          <Button
            onPress={this._onStartRecording}
            title='Start direct houndify voice'
            color='#841584'
          />

          <Button
            onPress={this._onStopRecording}
            title='Stop direct houndify recording'
            color='#841584'
          />
        </View>

        <View style={styles.row}>
          <Button
            onPress={this._voiceToTextStart}
            title='Start voice to text'
            color='#791edb'
          />
          <Button
            onPress={this._voiceToTextStop}
            title='Stop voice to text'
            color='#791edb'
          />
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  row: {
    flex: 1,
    flexDirection: 'row',
    backgroundColor: '#fff',
    justifyContent: 'center',
  },
});
