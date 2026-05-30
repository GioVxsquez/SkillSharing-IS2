import React, { useEffect, useState } from 'react';
import { ActivityIndicator, View } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import AsyncStorage from '@react-native-async-storage/async-storage';

import LoginScreen              from '../screens/LoginScreen';
import RegisterScreen           from '../screens/RegisterScreen';
import HomeScreen               from '../screens/HomeScreen';
import DetalleSesionScreen      from '../screens/DetalleSesionScreen';
import MisSesionesScreen        from '../screens/MisSesionesScreen';
import CrearSesionScreen        from '../screens/CrearSesionScreen';
import InvitacionesScreen       from '../screens/InvitacionesScreen';
import PerfilScreen             from '../screens/PerfilScreen';
import InvitarAsistentesScreen  from '../screens/InvitarAsistentesScreen';

const Stack = createNativeStackNavigator();

export default function AppNavigator() {
  const [cargando, setCargando]         = useState(true);
  const [tokenGuardado, setTokenGuardado] = useState<string | null>(null);

  // Al abrir la app, revisar si ya hay sesión activa
  useEffect(() => {
    AsyncStorage.getItem('userToken').then((t) => {
      setTokenGuardado(t);
      setCargando(false);
    });
  }, []);

  if (cargando) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#1B3A6B' }}>
        <ActivityIndicator size="large" color="#FFD700" />
      </View>
    );
  }

  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName={tokenGuardado ? 'Home' : 'Login'}
        screenOptions={{ headerShown: false }}
      >
        {/* Autenticación */}
        <Stack.Screen name="Login"    component={LoginScreen} />
        <Stack.Screen name="Register" component={RegisterScreen} />

        {/* App principal */}
        <Stack.Screen name="Home"          component={HomeScreen} />
        <Stack.Screen name="DetalleSesion" component={DetalleSesionScreen} />
        <Stack.Screen name="MisSesiones"   component={MisSesionesScreen} />
        <Stack.Screen name="CrearSesion"   component={CrearSesionScreen} />
        <Stack.Screen name="Invitaciones"       component={InvitacionesScreen} />
        <Stack.Screen name="InvitarAsistentes"  component={InvitarAsistentesScreen} />
        <Stack.Screen name="Perfil"             component={PerfilScreen} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
