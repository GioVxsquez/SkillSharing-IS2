import React, { useState, useEffect } from 'react';
import {
  View, Text, StyleSheet, TouchableOpacity, Alert,
  ActivityIndicator, StatusBar, FlatList,
} from 'react-native';
import * as DocumentPicker from 'expo-document-picker';
import { api } from '../api/config';

// US05: Subir material educativo a una sesion
// US27: Listar materiales subidos (vista previa del instructor)
export default function SubirMaterialScreen({ route, navigation }: any) {
  const { sesionId, sesionTitulo } = route.params;
  const [materiales, setMateriales] = useState<any[]>([]);
  const [subiendo, setSubiendo]     = useState(false);
  const [loading, setLoading]       = useState(true);

  const cargarMateriales = async () => {
    try {
      const resp = await api.get(`/materiales/sesion/${sesionId}`);
      setMateriales(resp.data.data || []);
    } catch {
      Alert.alert('Error', 'No se pudieron cargar los materiales.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarMateriales();
  }, [sesionId]);

  // US05: cargar archivo desde el dispositivo
  const handleSubirDocumento = async () => {
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation'],
        copyToCacheDirectory: true,
      });

      if (result.canceled) return;

      const file = result.assets[0];
      setSubiendo(true);

      const formData = new FormData();
      formData.append('archivo', {
        uri: file.uri,
        name: file.name,
        type: file.mimeType || 'application/octet-stream',
      } as any);

      const resp = await api.post(`/materiales/sesion/${sesionId}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      if (resp.data.ok) {
        Alert.alert('Éxito', 'Material subido correctamente.');
        cargarMateriales();
      }
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.mensaje || 'No se pudo subir el archivo.');
    } finally {
      setSubiendo(false);
    }
  };

  const handleEliminar = (materialId: number, nombre: string) => {
    Alert.alert(
      'Eliminar material',
      `¿Eliminar el archivo "${nombre}"?`,
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Eliminar',
          style: 'destructive',
          onPress: async () => {
            try {
              await api.delete(`/materiales/${materialId}`);
              setMateriales((prev) => prev.filter((m) => m.materialId !== materialId));
            } catch {
              Alert.alert('Error', 'No se pudo eliminar el material.');
            }
          },
        },
      ]
    );
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1B3A6B" />

      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>← Volver</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Materiales</Text>
        <Text style={styles.headerSubtitulo} numberOfLines={1}>{sesionTitulo}</Text>
      </View>

      <View style={styles.infoBox}>
        <Text style={styles.infoTexto}>
          📎 Sube archivos PDF, PPT o documentos Word para que tus aprendices los descarguen antes y durante la sesión.
        </Text>
      </View>

      {loading ? (
        <ActivityIndicator size="large" color="#1B3A6B" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={materiales}
          keyExtractor={(item) => String(item.materialId)}
          contentContainerStyle={styles.lista}
          ListEmptyComponent={
            <View style={styles.vacio}>
              <Text style={styles.vacioIcono}>📭</Text>
              <Text style={styles.vacioTexto}>Aún no hay materiales subidos para esta sesión.</Text>
            </View>
          }
          renderItem={({ item }) => (
            <View style={styles.card}>
              <View style={styles.cardLeft}>
                <Text style={styles.cardIcono}>📄</Text>
                <View style={{ flex: 1 }}>
                  <Text style={styles.cardNombre}>{item.nombre}</Text>
                  <Text style={styles.cardTipo}>{item.tipoArchivo} · {item.fechaSubida ? new Date(item.fechaSubida).toLocaleDateString('es-PE') : '-'}</Text>
                </View>
              </View>
              <TouchableOpacity onPress={() => handleEliminar(item.materialId, item.nombre)}>
                <Text style={styles.eliminarTexto}>✕</Text>
              </TouchableOpacity>
            </View>
          )}
          ListFooterComponent={
            <TouchableOpacity
              style={[styles.botonSubir, subiendo && { opacity: 0.7 }]}
              onPress={handleSubirDocumento}
              disabled={subiendo}
            >
              {subiendo
                ? <ActivityIndicator color="#fff" />
                : <Text style={styles.botonSubirTexto}>+ Subir archivo</Text>}
            </TouchableOpacity>
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F9FC' },
  header: { backgroundColor: '#1B3A6B', paddingTop: 48, paddingBottom: 16, paddingHorizontal: 20 },
  backTexto: { color: '#C8D8F0', fontSize: 14, marginBottom: 4, fontWeight: '600' },
  headerTitulo: { color: '#FFFFFF', fontSize: 18, fontWeight: '700' },
  headerSubtitulo: { color: '#C8D8F0', fontSize: 13, marginTop: 2 },
  infoBox: { backgroundColor: '#EBF4FF', margin: 16, padding: 14, borderRadius: 10, borderLeftWidth: 3, borderLeftColor: '#1B3A6B' },
  infoTexto: { fontSize: 13, color: '#2C5282', lineHeight: 20 },
  lista: { padding: 16, paddingBottom: 40 },
  vacio: { alignItems: 'center', marginTop: 48 },
  vacioIcono: { fontSize: 48, marginBottom: 12 },
  vacioTexto: { fontSize: 15, color: '#718096', textAlign: 'center' },
  card: { backgroundColor: '#FFFFFF', borderRadius: 12, padding: 14, marginBottom: 10, flexDirection: 'row', alignItems: 'center', shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.05, shadowRadius: 4, elevation: 2 },
  cardLeft: { flex: 1, flexDirection: 'row', alignItems: 'center', gap: 12 },
  cardIcono: { fontSize: 24 },
  cardNombre: { fontSize: 14, fontWeight: '700', color: '#1A202C' },
  cardTipo: { fontSize: 12, color: '#718096', marginTop: 2 },
  eliminarTexto: { color: '#E53E3E', fontSize: 18, fontWeight: '700', paddingHorizontal: 8 },
  botonSubir: { marginTop: 16, backgroundColor: '#1B3A6B', paddingVertical: 16, borderRadius: 12, alignItems: 'center' },
  botonSubirTexto: { color: '#FFFFFF', fontWeight: '700', fontSize: 16 },
});
