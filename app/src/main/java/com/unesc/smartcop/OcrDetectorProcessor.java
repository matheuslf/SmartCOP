/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.unesc.smartcop;

import android.util.SparseArray;
import android.widget.Button;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.unesc.smartcop.camera.GraphicOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    /*
    Variaveis para controle do detector.
    */
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private TextBlock item = null;
    private TextBlock itemestadoEncontrado = null;
    private TextBlock itemplacaEncontrada = null;

    /*
    Indicativo de placa e estado encontrada.
    */
    public boolean encontrouPlaca = false;
    public boolean encontrouEstado = false;

    /*
    Lista de placas para validação da placa correta.
    */
    private List<String> listaPlacas = new ArrayList<String>();

    /*
    Lista de estado.
    */
    private ArrayList<String> estado = new ArrayList<String>();

    /*
    Controle de placa e estado.
    */
    private String placa = "";
    private String estadoPlaca = "";

    /*
    Botões de saída do detector.
    */
    private Button io_btn_capturar;
    private Button io_btn_saida;

    /*
    Contador de letras e numeros inválidos.
    */
    private int contaInvalidos = 0;

    /*
    Hash para controle de entrada e saida dos veículos.
    */
    private HashMap<String, Date> lo_timer = new HashMap<String, Date>();

    /*
    Construtor padrão de detecção.
    */
    public OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, Button btnCapturar, Button btnSaida) {
        mGraphicOverlay = ocrGraphicOverlay;

        /*
        Armazena as opções de saída.
        */
        io_btn_capturar = btnCapturar;
        io_btn_saida = btnSaida;

        /*
        Carrega a lista de estado.
        */
        estado.add("AC-");
        estado.add("AL-");
        estado.add("AP-");
        estado.add("AM-");
        estado.add("BA-");
        estado.add("CE-");
        estado.add("DF-");
        estado.add("ES-");
        estado.add("GO-");
        estado.add("MA-");
        estado.add("MT-");
        estado.add("MS-");
        estado.add("MG-");
        estado.add("PA-");
        estado.add("PB-");
        estado.add("PR-");
        estado.add("PE-");
        estado.add("PI-");
        estado.add("RJ-");
        estado.add("RN-");
        estado.add("RS-");
        estado.add("RO-");
        estado.add("RR-");
        estado.add("SC-");
        estado.add("SP-");
        estado.add("SE-");
        estado.add("TO-");

    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {

        SparseArray<TextBlock> items = detections.getDetectedItems();

        /*
        Se tem itens a processar ...
         */
        if (items.size() > 0) {

            /*
            Se não encontrou placa ou não encontrou estado.
             */
            if (!encontrouPlaca || !encontrouEstado) {

                loop:
                for (int i = 0; i < items.size(); ++i) {

                    /*
                    Recupera o item encontrado durante a captura ...
                     */
                    item = items.valueAt(i);

                    /*
                    Se encontrou a placa e adicionou na lista de placa ...
                     */
                    if (PlacaAdicionar()) {

                        /*
                        Pinta o gráfico no display do celular ...
                         */
                        GraficoPlacaPintar();
                    }

                    /*
                    Se não encontrou a placa ...
                     */
                    else {

                        /*
                        Se encontrou o estado ...
                         */
                        if (EstadoEncontrar()) {

                             /*
                            Pinta o gráfico no display do celular ...
                             */
                            GraficoEstadoPintar();

                        }

                        /*
                        Se não encontrou o estado nem a placa ...
                         */
                        else {

                            contaInvalidos++;

                            /*
                            Pinta o gráfico no display do celular ...
                             */
                            GraficoPintar();

                        }
                    }
                }

                /*
                Encontra a placa presente no Detector ...
                 */
                PlacaEncontrar();
            }

            /*
            Se encontrou placa e estado ...
             */
            else {

                if (lo_timer.size() == 0) {
                    lo_timer.put(placa, new Date());

                    /*
                    Simula a ação do botão ...
                     */
                    io_btn_capturar.callOnClick();
                }
            }

        }

        /*
        Se não tem nada no detector ...
         */
        else {

            encontrouPlaca = false;
            encontrouEstado = false;

            /*
            Se tem a placa com data de entrada ...
             */
            if (lo_timer.remove(placa) != null) {
                io_btn_saida.callOnClick();
            }
        }
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }

    /*
    Método responsável por adicionar a placa do veículo.
     */
    private boolean PlacaAdicionar() {

        boolean isAdd = false;

         /*
        Busca a placa e faz uso de expressão regular.
         */
        Pattern pattern = Pattern.compile("[a-zA-Z]{3,3}-\\d{4,4}");
        Matcher matcher = pattern.matcher(item.getValue().replace(" ","").trim());

        boolean b = false;
        while (b = matcher.find()) {
            if (b) {
                isAdd = true;
                placa = matcher.group();
                listaPlacas.add(placa);
            }
        }

        return isAdd;
    }

    /*
    Método responsável por encontrar a placa perfeita no Array de Placas.
     */
    private void PlacaEncontrar(boolean teste) {

        /*
        Busca a placa perfeita ...
         */
        if  (!placa.isEmpty() && listaPlacas.size() >= 5) {

            Set<String> uniqueSet = new HashSet<String>(listaPlacas);

            int maior_valor = 0;

            for (String temp : uniqueSet) {

                if (maior_valor < Collections.frequency(listaPlacas, temp)) {
                    maior_valor = Collections.frequency(listaPlacas, temp);
                    placa = temp;
                }
            }

            /*
            Tratamento do W ...
             */
            boolean encontrou_w = false;
            int posicao_w = 0;

            loop_w:
            for (int i = 0; i < listaPlacas.size(); i++) {
                if (listaPlacas.get(i).contains("W")) {
                    posicao_w = listaPlacas.get(i).indexOf("W");
                    encontrou_w = !placa.contains("W");
                    break loop_w;
                }
            }

            if (encontrou_w) {

                switch (posicao_w) {
                    case 0:
                        placa = "W"+placa.substring(1,placa.length());
                        break;
                    case 1:
                        placa = placa.substring(0,1)+"W"+placa.substring(2, placa.length());
                        break;
                    case 2:
                        placa = placa.substring(0,2)+"W"+placa.substring(3,placa.length());
                        break;
                }
            }

            encontrouPlaca = true;
        }

        else if (listaPlacas.size() > 20) {
            placa = "Inexistente";
            estadoPlaca = "Inexistente";
            encontrouPlaca = true;
            encontrouEstado = true;
        }
    }

    /*
    Método responsável por encontrar a placa perfeita no Array de Placas.
     */
    private void PlacaEncontrar() {

        /*
        Busca a placa perfeita ...
         */
        if  (!placa.isEmpty()) {

            /*
            Tratamento do W ...
             */
            boolean encontrou_w = false;
            int posicao_w = 0;

            loop_w:
            for (int i = 0; i < listaPlacas.size(); i++) {
                if (listaPlacas.get(i).contains("W")) {
                    posicao_w = listaPlacas.get(i).indexOf("W");
                    encontrou_w = !placa.contains("W");
                    break loop_w;
                }
            }

            if (encontrou_w) {

                switch (posicao_w) {
                    case 0:
                        placa = "W"+placa.substring(1,placa.length());
                        break;
                    case 1:
                        placa = placa.substring(0,1)+"W"+placa.substring(2, placa.length());
                        break;
                    case 2:
                        placa = placa.substring(0,2)+"W"+placa.substring(3,placa.length());
                        break;
                }
            }

            encontrouPlaca = true;
        }

        else if (listaPlacas.size() > 20) {
            placa = "Inexistente";
            estadoPlaca = "Inexistente";
            encontrouPlaca = true;
            encontrouEstado = true;
        }
    }

    /*
    Método responsável por encontrar o estado presente na placa do veículo.
     */
    private boolean EstadoEncontrar() {

        boolean rotina_encontrada = false;

        String ls_estado = null;

        if  (
                item.getValue().length()
                        >  2
                        &&
                        item.getValue().indexOf("-")
                                >   0
                )
        {
            ls_estado = item.getValue().substring(0,item.getValue().indexOf("-")+1).replace(" ","").trim();

            if (estado.contains(ls_estado) &&  estadoPlaca.isEmpty()) {
                rotina_encontrada = true;
                encontrouEstado = true;
                estadoPlaca = ls_estado.replace("-", "");
            }
        }

        return rotina_encontrada;
    }

    /*
    Método responsável por pintar o gráfico.
     */
    private void GraficoPintar() {

        if (contaInvalidos > 10) {
            mGraphicOverlay.clear();
            contaInvalidos = 0;
        }

        /*
        Limpa os grafícos e adiciona os itens.
         */
        OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
        graphic.setInvalidColor();
        mGraphicOverlay.add(graphic);
    }

    /*
    Método responsável por pintar o gráfico da placa.
     */
    private void GraficoPlacaPintar() {

        /*
        Limpa os grafícos e adiciona os itens.
         */
        mGraphicOverlay.clear();
        itemplacaEncontrada = item;
        OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
        graphic.setPlacaColor();
        mGraphicOverlay.add(graphic);

        if (itemestadoEncontrado != null) {
            graphic = new OcrGraphic(mGraphicOverlay, itemestadoEncontrado);
            graphic.setEstadoColor();
            mGraphicOverlay.add(graphic);
        }
    }

    /*
    Método responsável por pintar o gráfico da placa.
     */
    private void GraficoEstadoPintar() {

        /*
        Limpa os grafícos e adiciona os itens.
         */
        mGraphicOverlay.clear();
        itemestadoEncontrado = item;
        OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
        graphic.setEstadoColor();
        mGraphicOverlay.add(graphic);

        if (itemplacaEncontrada != null) {
            graphic = new OcrGraphic(mGraphicOverlay, itemplacaEncontrada);
            graphic.setPlacaColor();
            mGraphicOverlay.add(graphic);
        }
    }

    /*
    Retorna a placa encontrada.
     */
    public String placaGet() {
        return placa;
    }

    /*
    Retorna o estado encontrado.
     */
    public String estadoGet() {
        return estadoPlaca;
    }
}
